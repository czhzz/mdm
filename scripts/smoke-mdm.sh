#!/usr/bin/env bash
#
# MDM 1.0.0 端到端冒烟脚本（模型→编码→维护→审核→质量→分发→对外接口）
# 用法: bash scripts/smoke-mdm.sh [BASE_URL, 默认 http://localhost:8080]
# 依赖: curl jq openssl; 需预先关闭验证码(sys.account.captchaEnabled=false)
# 分发订阅方 mock: 容器 mdm-mock-push（监听 9999 返回 200）
#
set -uo pipefail
BASE=${1:-http://localhost:8080}
TOKEN=""
CURL=(-s -m 15 -H "Content-Type: application/json")
PASS=0; FAIL=0
TMP=$(mktemp -d)

ok()  { echo "  PASS: $1"; PASS=$((PASS+1)); }
bad() { echo "  FAIL: $1"; FAIL=$((FAIL+1)); }
resp() { echo; echo "       response: $(cat "$TMP/resp.json" | head -c 300)"; }
req() { # method path [json] -> $TMP/resp.json + $TMP/code
  local m=$1 p=$2 b=${3:-}
  curl "${CURL[@]}" -o "$TMP/resp.json" -w '%{http_code}' -X "$m" "$BASE$p" \
    ${TOKEN:+-H "Authorization: Bearer $TOKEN"} ${b:+-d "$b"} > "$TMP/code"
}
code() { cat "$TMP/code"; }
j() { jq -r "$1" "$TMP/resp.json"; }

echo "== $BASE =="

echo "[1/10] 登录"
req POST /login '{"username":"admin","password":"admin123"}'
TOKEN=$(j '.token // .data.token // empty')
[ -n "$TOKEN" ] && ok "登录成功" || { bad "登录无 token"; resp; }

echo "[2/10] 模型：示例对象存在"
req GET "/mdm/model/object/list?pageSize=100"
for obj in customer supplier; do
  j '.rows[]?.objectCode' | grep -q "^$obj$" && ok "对象 $obj 存在" || bad "缺少对象 $obj"
done

echo "[3/10] 编码规则：supplier 新增自动编码"
req POST /mdm/data/supplier '{"supplier_name":"冒烟供应商A","supplier_grade":"A","city":"杭州"}'
req GET "/mdm/data/supplier/list?pageNum=1&pageSize=10"
CODE=$(j '.rows[0].supplier_code // empty')
case "$CODE" in SUP-*) ok "自动编码: $CODE";; *) bad "未生成 SUP- 编码: $CODE"; resp;; esac

echo "[4/10] 审核（Flowable）：提交→通过→落库"
req POST /mdm/data/customer '{"cust_name":"冒烟客户A","cust_level":"A","phone":"13800000000"}'
MSG=$(j '.msg // empty')
[[ "$MSG" == *"已提交审核"* ]] && ok "customer 新增转待审核" || { bad "应提示已提交审核: $MSG"; resp; }
req GET "/mdm/audit/flowable/todo?pageSize=10"
TASK=$(j '[.rows[]?.id][0]')
[ "$TASK" != "null" ] && ok "Flowable 待办 task=$TASK" || bad "无待办任务"
req PUT "/mdm/audit/flowable/task/$TASK/approve" '{"comment":"冒烟通过"}'
[ "$(code)" = "200" ] && ok "审核通过" || bad "审核通过失败"
sleep 1
req GET "/mdm/data/customer/list?pageNum=1&pageSize=10"
j '.rows[]?.cust_name' | grep -q "冒烟客户A" && ok "审核通过后数据落库" || bad "审核通过后未出现数据"
CID=$(j '[.rows[]? | select(.cust_name=="冒烟客户A") | .id][0]')

echo "[5/10] 血缘 + 关系 + 模板 + 大屏（1.1.0）"
req GET "/mdm/lineage/customer/$CID"
[ "$(code)" = "200" ] && ok "血缘查询 OK (source=$(j '.data.source.type // "?"'))" || bad "血缘查询失败"
req POST /mdm/relation '{"sourceObjectCode":"supplier","targetObjectCode":"customer","relationType":"ONE_TO_MANY","sourceFieldCode":"city","cascadeRule":"RESTRICT","isBidirectional":"1"}'
[ "$(code)" = "200" ] && ok "创建关系定义" || bad "创建关系失败"
req GET /mdm/relation/list?pageSize=10
j '.rows|length' | grep -qE '^[1-9]' && ok "关系列表查询 OK" || bad "关系列表为空"
req GET /mdm/template/list
j '.data|length' | grep -q 5 && ok "模板库 5 个模板" || bad "模板数量不为 5"
req GET /mdm/quality/dashboard
j '.data.overview.totalIssues' | grep -qE '^[0-9]+$' && ok "质量大屏聚合 OK" || bad "大屏聚合失败"
req GET /mdm/distribution/monitor
[ "$(code)" = "200" ] && ok "分发监控 OK" || bad "分发监控失败"

echo "[6/10] 质量：规则与重复检测"
# 动态解析 supplier 对象ID（不假设种子库 ID 顺序——存在手工创建对象时 ID 会偏移）
req GET "/mdm/model/object/list?pageSize=100"
SOBJ=$(j '[.rows[]? | select(.objectCode=="supplier") | .objectId][0]')
req POST /mdm/quality/rule "{\"objectId\":$SOBJ,\"ruleName\":\"供应商名称必填\",\"targetType\":\"ATTRIBUTE\",\"targetValue\":\"supplier_name\",\"ruleType\":\"REQUIRED\",\"status\":\"0\"}"
[ "$(code)" = "200" ] && ok "创建校验规则" || bad "创建规则失败"
req POST /mdm/quality/duplicate '{"objectCode":"supplier","fields":["supplier_name"]}'
[ "$(code)" = "200" ] && ok "重复检测执行" || bad "重复检测失败"

echo "[7/10] 分发：应用/配置/推送成功"
req POST /mdm/distribution/app '{"appName":"冒烟订阅系统"}'
APPNUM=$(j '.data?.appId // empty'); APPCRE=$(j '.data?.appid // empty'); SECRET=$(j '.data?.secret // empty')
[ -n "$APPCRE" ] && ok "创建应用 appid=$APPCRE" || { bad "创建应用失败"; resp; }
req POST /mdm/distribution/config "{\"appId\":$APPNUM,\"objectId\":$SOBJ,\"triggerType\":\"IMMEDIATE\",\"endpointUrl\":\"http://mdm-mock-push:9999/mdm/push\",\"enabled\":\"1\"}"
[ "$(code)" = "200" ] && ok "创建分发配置" || bad "创建分发配置失败"
req GET "/mdm/data/supplier/list?pageNum=1&pageSize=1"
CID2=$(j '[.rows[]?.id][0]')
req PUT "/mdm/data/supplier/$CID2" '{"city":"上海"}'
rec_status() { req GET "/mdm/distribution/record/list?pageNum=1&pageSize=5"; j '[.rows[]? | select(.actionType=="UPDATE") | .status][0]'; }
ST="0"; for i in 1 2 3 4 5; do ST=$(rec_status); [ "$ST" = "1" ] || [ "$ST" = "2" ] && break; sleep 2; done
[ "$ST" = "1" ] && ok "异步推送成功" || { bad "推送未成功 status=$ST"; }

echo "[8/10] 分发：失败→修正→重推"
req GET "/mdm/distribution/config/list?pageSize=50"
# 取最新一条指向 mock 的分发配置（历史多轮运行会残留旧配置，不能取第一条）
DIST=$(j '[.rows[]? | select(.endpointUrl=="http://mdm-mock-push:9999/mdm/push") | .distId] | max')
req PUT "/mdm/distribution/config" "{\"distId\":$DIST,\"appId\":$APPNUM,\"objectId\":$SOBJ,\"triggerType\":\"IMMEDIATE\",\"endpointUrl\":\"http://mdm-mock-push:9998/no\",\"enabled\":\"1\"}"
[ "$(code)" = "200" ] || { bad "切换死地址失败"; }
req PUT "/mdm/data/supplier/$CID2" '{"city":"杭州"}'
REC="null"; for i in 1 2 3 4 5; do
  req GET "/mdm/distribution/record/list?pageNum=1&pageSize=10"
  REC=$(j '[.rows[]? | select(.status=="2") | .recordId][0]'); [ "$REC" != "null" ] && break; sleep 2
done
[ "$REC" != "null" ] && ok "失败记录生成 rec=$REC" || { bad "未产生失败记录"; }
req PUT "/mdm/distribution/config" "{\"distId\":$DIST,\"appId\":$APPNUM,\"objectId\":$SOBJ,\"triggerType\":\"IMMEDIATE\",\"endpointUrl\":\"http://mdm-mock-push:9999/mdm/push\",\"enabled\":\"1\"}"
req PUT "/mdm/distribution/record/retry/$REC"
sleep 2
req GET "/mdm/distribution/record/list?pageNum=1&pageSize=8"
j "[.rows[]? | select(.recordId==$REC) | .status][0]" | grep -qx '1' && ok "重推后成功" || bad "重推后仍失败"

echo "[9/10] Excel：模板/导入/导出"
curl -s -m 20 -o "$TMP/tpl.xlsx" -w '%{http_code}' -H "Authorization: Bearer $TOKEN" \
  "$BASE/mdm/data/supplier/template" > "$TMP/code"
CT=$(file "$TMP/tpl.xlsx" | grep -ci "microsof*\|zip" || true)
[ "$(code)" = "200" ] && [ -s "$TMP/tpl.xlsx" ] && ok "模板下载 OK ($(stat -f%z "$TMP/tpl.xlsx")B)" || bad "模板下载失败"
curl -s -m 30 -o "$TMP/resp.json" -w '%{http_code}' -H "Authorization: Bearer $TOKEN" \
  -F "file=@$TMP/tpl.xlsx" "$BASE/mdm/data/supplier/import" > "$TMP/code"
j '.msg // empty' | grep -q "导入完成" && ok "空模板导入校验 OK: $(j '.msg')" || bad "导入失败: $(j '.msg')"
curl -s -m 30 -o "$TMP/exp.xlsx" -w '%{http_code}' -H "Authorization: Bearer $TOKEN" \
  "$BASE/mdm/data/supplier/export" > "$TMP/code"
[ "$(code)" = "200" ] && [ -s "$TMP/exp.xlsx" ] && ok "导出 OK ($(stat -f%z "$TMP/exp.xlsx")B)" || bad "导出失败"

echo "[10/10] 对外接口：签名鉴权"
TS=$(date +%s)000
SIGN=$(printf '%s|%s|%s' "$APPCRE" "$TS" "" | openssl dgst -sha256 -hmac "$SECRET" -hex | awk '{print $2}')
curl -s -m 10 -o "$TMP/resp.json" -w '%{http_code}' \
  -H "X-Appid: $APPCRE" -H "X-Timestamp: $TS" -H "X-Sign: $SIGN" \
  "$BASE/open/mdm/data?objectCode=supplier" > "$TMP/code"
j '.code' | grep -q 200 && ok "有效签名查询 200 (total=$(j '.data.total // "?"'), rows=$(j '.data.rows|length' 2>/dev/null || echo 0))" || bad "有效签名被拒: $(j '.msg')"
curl -s -m 10 -o /dev/null -w '%{http_code}' \
  -H "X-Appid: bad" -H "X-Timestamp: $TS" -H "X-Sign: x" \
  "$BASE/open/mdm/data?objectCode=supplier" | grep -q 401 && ok "无效凭证返回 401" || bad "无效凭证未返回 401"

echo ""
echo "== 结果: PASS=$PASS FAIL=$FAIL =="
rm -rf "$TMP"
[ "$FAIL" -eq 0 ]