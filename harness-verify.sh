#!/bin/bash
# harness-verify.sh — Harness 模板完整性验证脚本
# 用途：验证所有必要文件存在，确保 Harness 模板可直接使用
# 使用方式：bash harness-verify.sh

set -euo pipefail

ERRORS=0
CHECKS=0

GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

check_file() {
  CHECKS=$((CHECKS + 1))
  if [ ! -f "$1" ]; then
    echo -e "${RED}❌ 缺失文件: $1${NC}"
    ERRORS=$((ERRORS + 1))
  else
    echo -e "${GREEN}✅ $1${NC}"
  fi
}

check_dir() {
  CHECKS=$((CHECKS + 1))
  if [ ! -d "$1" ]; then
    echo -e "${RED}❌ 缺失目录: $1${NC}"
    ERRORS=$((ERRORS + 1))
  else
    echo -e "${GREEN}✅ $1/${NC}"
  fi
}

echo ""
echo "=================================================="
echo "  Harness 模板完整性验证"
echo "=================================================="
echo ""

# ── 1. Guidance 文件（5个）──────────────────────────────
echo -e "${YELLOW}[1/7] Guidance 文件体系${NC}"
check_file ".codex/guidance/java-engineering-standards.md"
check_file ".codex/guidance/testing-quality-standards.md"
check_file ".codex/guidance/api-doc-sync-protocol.md"
check_file ".codex/guidance/ai-collaboration-protocol.md"
check_file ".codex/guidance/project-lifecycle.md"
check_file ".codex/specs/spring-boot-harness-template/.config.codex"
echo ""

# ── 2. Checklists（4个）─────────────────────────────────────
echo -e "${YELLOW}[2/7] Checklists 检查清单体系${NC}"
check_file ".codex/checklists/api-doc-sync-check.json"
check_file ".codex/checklists/layer-constraint-check.json"
check_file ".codex/checklists/test-coverage-reminder.json"
check_file ".codex/checklists/maven-profile-check.json"
echo ""

# ── 3. Maven 构建配置──────────────────────────────────
echo -e "${YELLOW}[3/7] Maven 构建配置${NC}"
check_file "pom.xml"
check_file "config/checkstyle/checkstyle.xml"
check_file "config/checkstyle/checkstyle-strict.xml"
check_file "config/spotbugs/exclude.xml"
echo ""

# ── 4. CI/CD 配置─────────────────────────────────────
echo -e "${YELLOW}[4/7] CI/CD 配置${NC}"
check_file ".github/workflows/ci-verify.yml"
echo ""

# ── 5. harness-collab 文档体系────────────────────────
echo -e "${YELLOW}[5/7] harness-collab 文档体系${NC}"
check_file "harness-collab/README.md"
check_file "harness-collab/AGENTS.md"
check_file "harness-collab/func.md"
check_dir  "harness-collab/01-product-specs/templates"
check_file "harness-collab/01-product-specs/templates/product-spec-template.md"
check_dir  "harness-collab/02-design-docs/templates"
check_file "harness-collab/02-design-docs/templates/design-doc-template.md"
check_dir  "harness-collab/03-exec-plans/templates"
check_file "harness-collab/03-exec-plans/templates/exec-plan-template.md"
check_dir  "harness-collab/04-api-docs/templates"
check_file "harness-collab/04-api-docs/templates/api-doc-template.md"
check_file "harness-collab/05-methodology/architecture-constraints.md"
check_file "harness-collab/05-methodology/dev-workflow.md"
check_file "harness-collab/05-methodology/ai-delivery-playbook.md"
check_file "harness-collab/06-adapters/bootstrap-guide.md"
check_file "harness-collab/06-adapters/retrofit-guide.md"
echo ""

# ── 6. 根目录文档────────────────────────────────────
echo -e "${YELLOW}[6/7] 根目录文档${NC}"
check_file "README.md"
check_file "AGENTS.md"
echo ""

# ── 7. Guidance 元数据验证────────────────────────────
echo -e "${YELLOW}[7/7] Guidance 文件元数据验证（codex-harness: required）${NC}"
GUIDANCE_COUNT=0
for f in .codex/guidance/*.md; do
  if [ -f "$f" ]; then
    if grep -q "codex-harness: required" "$f"; then
      echo -e "${GREEN}✅ $f 包含 codex-harness: required${NC}"
      GUIDANCE_COUNT=$((GUIDANCE_COUNT + 1))
    else
      echo -e "${RED}❌ $f 缺少 codex-harness: required 元数据${NC}"
      ERRORS=$((ERRORS + 1))
    fi
    CHECKS=$((CHECKS + 1))
  fi
done
echo ""

# ── 汇总──────────────────────────────────────────────
echo "=================================================="
if [ $ERRORS -eq 0 ]; then
  echo -e "${GREEN}✅ 验证通过！共完成 $CHECKS 项检查，全部通过。${NC}"
  echo -e "${GREEN}   Guidance 文件数量：$GUIDANCE_COUNT（均包含 codex-harness: required）${NC}"
  echo ""
  echo "Harness 模板已就绪，可直接使用。"
  echo "新建 Spring Boot 子项目后，在 Codex 中打开工作区根目录即可按 AGENTS.md 约束开发。"
else
  echo -e "${RED}❌ 验证失败！共完成 $CHECKS 项检查，发现 $ERRORS 个问题。${NC}"
  echo ""
  echo "请补充缺失的文件后重新运行验证。"
  exit 1
fi
echo "=================================================="
echo ""
