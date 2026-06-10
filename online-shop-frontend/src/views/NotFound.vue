<template>
  <section class="page result-page">
    <div class="result-panel">
      <el-result icon="warning" title="页面不存在" sub-title="当前访问的地址没有对应页面。">
        <template #extra>
          <el-button type="primary" @click="$router.push('/products')">返回商品列表</el-button>
        </template>
      </el-result>
    </div>
  </section>
</template>
