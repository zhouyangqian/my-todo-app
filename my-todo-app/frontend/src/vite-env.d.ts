// vite-env.d.ts - Vite 环境变量的 TypeScript 类型定义，声明 .env 文件中可用的变量

// 环境变量类型定义，对应 .env 文件中以 VITE_ 开头的变量
interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string  // 后端 API 基础地址
  readonly VITE_APP_TITLE: string     // 应用标题
}

// 扩展 ImportMeta 类型，使 import.meta.env 具有类型提示
interface ImportMeta {
  readonly env: ImportMetaEnv
}
