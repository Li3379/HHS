<template>
  <div class="container home">
    <section class="banner">
      <div class="left">
        <h1>打造健康生活的AI社区</h1>
        <p>发现、分享、捕获生活小技巧，借助AI健康顾问守护你的健康。</p>
        <el-button type="primary" size="large" @click="handlePrimaryAction">
          {{ isLoggedIn ? '立即分享' : '登录后开始分享' }}
        </el-button>
      </div>
      <div class="right">
        <!-- Banner图片暂时移除，避免CDN加载失败 -->
        <!-- <el-image src="/assets/health-banner.svg" fit="contain" /> -->
      </div>
    </section>

    <section class="tip-list">
      <div class="tip-list__header">
        <h2>精选技巧</h2>
        <el-select v-model="category" placeholder="选择分类" clearable style="width: 200px">
          <el-option label="全部" value="" />
          <el-option label="饮食" value="diet" />
          <el-option label="运动" value="fitness" />
          <el-option label="睡眠" value="sleep" />
          <el-option label="心理" value="mental" />
        </el-select>
      </div>
      <TipCard v-for="tip in tips" :key="tip.id" :tip="tip" @click="() => toDetail(tip.id)" />
      <el-pagination
        v-if="total > 0"
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 30, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
        style="margin-top: 24px; justify-content: center;"
      />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, computed } from "vue";
import { useRouter } from "vue-router";
import TipCard from "@/components/TipCard.vue";
import { fetchTips } from "@/api/healthTip";
import { useUserStore } from "@/store/user";

const router = useRouter();
const tips = ref([]);
const category = ref("");
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const userStore = useUserStore();
const isLoggedIn = computed(() => !!userStore.token);

const loadTips = async () => {
  const data = await fetchTips({ 
    category: category.value,
    page: currentPage.value,
    size: pageSize.value
  });
  tips.value = data?.records || [];
  total.value = data?.total || 0;
};

const handlePageChange = (page) => {
  currentPage.value = page;
  loadTips();
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadTips();
};

const toDetail = (id) => {
  router.push(`/tips/${id}`);
};

const handlePrimaryAction = () => {
  if (isLoggedIn.value) {
    router.push("/publish");
  } else {
    router.push({ path: "/login", query: { redirect: "/publish" } });
  }
};

// 监听分类变化，重新加载技巧列表
watch(category, () => {
  currentPage.value = 1;
  loadTips();
});

onMounted(() => {
  loadTips();
});
</script>

<style scoped>
.home {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.banner {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  padding: 32px;
  background: linear-gradient(135deg, #e8f3ff 0%, #f1f9ff 100%);
  border-radius: 16px;
}

.banner .left h1 {
  font-size: 32px;
  margin-bottom: 16px;
}

.banner .left p {
  color: #606266;
  margin-bottom: 24px;
  font-size: 16px;
}

.tip-list__header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>
