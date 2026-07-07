<template>
  <section class="page">
    <div class="section-title">
      <div>
        <h1>个人中心</h1>
        <p class="muted" v-if="profile">{{ profile.nickname }} · {{ profile.username }}</p>
      </div>
      <el-button @click="loadAll">刷新</el-button>
    </div>

    <div class="profile-grid">
      <div class="info-panel">
        <h2>账户概览</h2>
        <el-descriptions v-if="profile" :column="1" border>
          <el-descriptions-item label="用户名">{{ profile.username }}</el-descriptions-item>
          <el-descriptions-item label="昵称">{{ profile.nickname }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ profile.phone || '未填写' }}</el-descriptions-item>
          <el-descriptions-item label="积分">{{ profile.points }}</el-descriptions-item>
          <el-descriptions-item label="订单数">{{ profile.orderCount }}</el-descriptions-item>
        </el-descriptions>
      </div>

      <el-form class="info-panel" :model="passwordForm" label-position="top">
        <h2>修改密码</h2>
        <el-form-item label="原密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-button type="primary" @click="changePassword">保存新密码</el-button>
      </el-form>
    </div>

    <div class="info-panel">
      <div class="section-title compact">
        <h2>收货地址</h2>
        <el-button type="primary" @click="startCreate">新增地址</el-button>
      </div>
      <el-table :data="addresses" empty-text="暂无地址">
        <el-table-column prop="receiverName" label="收货人" width="120" />
        <el-table-column prop="receiverPhone" label="联系方式" width="140" />
        <el-table-column prop="receiverAddress" label="地址" min-width="260" />
        <el-table-column label="默认" width="90">
          <template #default="{ row }">
            <el-tag v-if="row.defaultAddress" type="success">默认</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="160">
          <template #default="{ row }">
            <el-button link type="primary" @click="startEdit(row)">编辑</el-button>
            <el-button link type="danger" @click="removeAddress(row.id)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="addressDialogVisible" :title="editingAddressId ? '编辑地址' : '新增地址'" width="420px">
      <el-form :model="addressForm" label-position="top">
        <el-form-item label="收货人">
          <el-input v-model="addressForm.receiverName" />
        </el-form-item>
        <el-form-item label="联系方式">
          <el-input v-model="addressForm.receiverPhone" />
        </el-form-item>
        <el-form-item label="收货地址">
          <el-input v-model="addressForm.receiverAddress" type="textarea" :rows="3" />
        </el-form-item>
        <el-checkbox v-model="addressForm.defaultAddress">设为默认地址</el-checkbox>
      </el-form>
      <template #footer>
        <el-button @click="addressDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveAddress">保存</el-button>
      </template>
    </el-dialog>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { addressApi, userApi } from '../api'
import { clearAuth } from '../auth'

const router = useRouter()
const profile = ref(null)
const addresses = ref([])
const addressDialogVisible = ref(false)
const editingAddressId = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: ''
})
const addressForm = reactive({
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  defaultAddress: false
})

const loadAll = async () => {
  const [profileResult, addressResult] = await Promise.all([
    userApi.profile(),
    addressApi.list()
  ])
  profile.value = profileResult
  addresses.value = addressResult
}

const changePassword = async () => {
  await userApi.changePassword(passwordForm)
  ElMessage.success('密码已修改，请重新登录')
  clearAuth()
  router.push('/login')
}

const startCreate = () => {
  editingAddressId.value = null
  Object.assign(addressForm, {
    receiverName: '',
    receiverPhone: '',
    receiverAddress: '',
    defaultAddress: !addresses.value.length
  })
  addressDialogVisible.value = true
}

const startEdit = (address) => {
  editingAddressId.value = address.id
  Object.assign(addressForm, address)
  addressDialogVisible.value = true
}

const saveAddress = async () => {
  if (editingAddressId.value) {
    await addressApi.update(editingAddressId.value, addressForm)
    ElMessage.success('地址已更新')
  } else {
    await addressApi.add(addressForm)
    ElMessage.success('地址已新增')
  }
  addressDialogVisible.value = false
  await loadAll()
}

const removeAddress = async (id) => {
  await addressApi.remove(id)
  ElMessage.success('地址已删除')
  await loadAll()
}

onMounted(loadAll)
</script>
