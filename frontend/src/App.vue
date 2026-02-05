<template>
  <el-config-provider :locale="locale">
    <MainLayout>
      <router-view v-slot="{ Component }">
        <transition name="fade-slide" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </MainLayout>
  </el-config-provider>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import MainLayout from '@/components/Layout/MainLayout.vue'

const locale = ref(zhCn)
</script>
