<template>
  <div class="container publish">
    <el-card shadow="hover">
      <h2>发布健康生活技巧</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px" class="publish-form">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入技巧标题" />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类">
            <el-option label="饮食" value="diet" />
            <el-option label="运动" value="fitness" />
            <el-option label="睡眠" value="sleep" />
            <el-option label="心理" value="mental" />
          </el-select>
        </el-form-item>
        <el-form-item label="标签">
          <el-select v-model="form.tags" placeholder="AI智能推荐标签" multiple filterable allow-create>
            <el-option v-for="tag in suggestTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
          <el-button type="primary" link @click="handleAIClassify">AI智能分类</el-button>
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="10" placeholder="请输入详细内容" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" :loading="loading" @click="handleSubmit">发布</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from "vue";
import { useRouter } from "vue-router";
import { ElMessage } from "element-plus";
import { createTip } from "@/api/healthTip";
import { classifyContent } from "@/api/ai";

const router = useRouter();
const formRef = ref();
const loading = ref(false);
const suggestTags = ref([]);

const form = reactive({
  title: "",
  category: "",
  tags: [],
  content: ""
});

const rules = {
  title: [{ required: true, message: "请输入标题", trigger: "blur" }],
  category: [{ required: true, message: "请选择分类", trigger: "change" }],
  content: [{ required: true, message: "请输入内容", trigger: "blur" }]
};

const handleAIClassify = async () => {
  if (!form.title || !form.content) {
    ElMessage.warning("请先输入标题和内容，AI才能分析");
    return;
  }
  try {
    const data = await classifyContent({ 
      title: form.title, 
      content: form.content 
    });
    if (data) {
      form.category = data.category || form.category;
      suggestTags.value = data.tags || [];
      form.tags = Array.from(new Set([...form.tags, ...(data.tags || [])]));
      // 显示摘要（如果有）
      if (data.summary) {
        ElMessage.success(`AI分析成功！${data.summary}`);
      } else {
        ElMessage.success("AI智能推荐成功");
      }
    }
  } catch (error) {
    console.error("AI分类失败:", error);
    ElMessage.error(error.message || "AI分类暂时不可用");
  }
};

const handleSubmit = () => {
  formRef.value.validate(async (valid) => {
    if (!valid) return;
    loading.value = true;
    try {
      const data = {
        ...form,
        tags: form.tags.length > 0 ? form.tags : ["默认标签"]
      };
      await createTip(data);
      ElMessage.success("发布成功！正在跳转至首页...");
      
      // 延迟跳转，确保用户看到成功提示
      setTimeout(() => {
        router.push("/");
      }, 1000);
      
    } catch (error) {
      console.error("发布失败:", error);
    } finally {
      loading.value = false;
    }
  });
};

const handleReset = () => {
  Object.assign(form, {
    title: "",
    category: "",
    tags: [],
    content: ""
  });
  suggestTags.value = [];
  formRef.value?.clearValidate();
};
</script>

<style scoped>
.publish {
  margin-top: 24px;
}

.publish-form {
  max-width: 800px;
}
</style>
