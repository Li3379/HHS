<template>
  <div class="container home">
    <section class="banner">
      <div class="left">
        <h1>打造健康生活的AI社区</h1>
        <p>发现、分享、捕获生活小技巧，借助AI健康顾问守护你的健康。</p>
        <el-button type="primary" size="large" @click="toPublish">立即分享</el-button>
      </div>
      <div class="right">
        <el-image src="https://cdn.jsdelivr.net/gh/health-hack-system/assets/health-banner.svg" fit="contain" />
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
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRouter } from "vue-router";
import TipCard from "@/components/TipCard.vue";
import { fetchTips } from "@/api/healthTip";

const router = useRouter();
const tips = ref([]);
const category = ref("");

const loadTips = async () => {
  const data = await fetchTips({ category: category.value });
  tips.value = data?.records || [];
};

const toDetail = (id) => {
  router.push(`/tips/${id}`);
};

const toPublish = () => {
  router.push("/publish");
};

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
