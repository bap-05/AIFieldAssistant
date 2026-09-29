package com.example.aifieldassistant.data.remote

// --- REQUEST MODELS ---
data class GeminiRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig
)

data class GenerationConfig(
    val responseMimeType: String = "application/json" // Ép Gemini trả về JSON chuẩn
)

data class Content(
    val parts: List<Part>
)

data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String // Chuỗi ảnh đã mã hóa Base64
)

// --- RESPONSE MODELS ---
data class GeminiResponse(
    val candidates: List<Candidate>?
)

data class Candidate(
    val content: Content?
)