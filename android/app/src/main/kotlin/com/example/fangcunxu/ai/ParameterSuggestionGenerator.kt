package com.example.fangcunxu.ai

data class ParameterSuggestion(
    val aperture: String,
    val focalLength: String,
    val iso: String,
    val shutterSpeed: String,
    val explanation: String
)

class ParameterSuggestionGenerator {
    
    fun generateSuggestions(
        score: Int,
        sceneType: com.example.fangcunxu.ai.SceneType,
        subjectProminence: Float
    ): ParameterSuggestion {
        val aperture = suggestAperture(score, sceneType, subjectProminence)
        val focalLength = suggestFocalLength(sceneType)
        val iso = suggestISO(score, sceneType)
        val shutterSpeed = suggestShutterSpeed(score, sceneType)
        val explanation = generateExplanation(score, sceneType)
        
        return ParameterSuggestion(
            aperture = aperture,
            focalLength = focalLength,
            iso = iso,
            shutterSpeed = shutterSpeed,
            explanation = explanation
        )
    }
    
    private fun suggestAperture(
        score: Int,
        sceneType: com.example.fangcunxu.ai.SceneType,
        subjectProminence: Float
    ): String {
        return when (sceneType) {
            com.example.fangcunxu.ai.SceneType.PORTRAIT -> {
                when {
                    subjectProminence > 0.7f -> "f/1.8 - f/2.8 (大光圈虚化背景)"
                    subjectProminence > 0.5f -> "f/2.8 - f/4.0 (中等光圈)"
                    else -> "f/4.0 - f/5.6 (小光圈保证清晰)"
                }
            }
            com.example.fangcunxu.ai.SceneType.LANDSCAPE -> {
                when {
                    score >= 80 -> "f/8 - f/11 (小光圈保证全景清晰)"
                    score >= 60 -> "f/5.6 - f/8 (中等光圈)"
                    else -> "f/4 - f/5.6 (适当光圈)"
                }
            }
            com.example.fangcunxu.ai.SceneType.NIGHT -> {
                when {
                    score >= 70 -> "f/1.8 - f/2.8 (最大光圈收集光线)"
                    else -> "f/2.8 - f/4.0 (大光圈)"
                }
            }
            com.example.fangcunxu.ai.SceneType.STREET -> {
                when {
                    score >= 70 -> "f/2.8 - f/4.0 (大光圈捕捉动态)"
                    else -> "f/4.0 - f/5.6 (中等光圈)"
                }
            }
            com.example.fangcunxu.ai.SceneType.MACRO -> {
                when {
                    subjectProminence > 0.6f -> "f/2.8 - f/4.0 (大光圈虚化)"
                    else -> "f/4.0 - f/5.6 (小光圈保证景深)"
                }
            }
            else -> "f/4.0 - f/8.0 (根据场景调整)"
        }
    }
    
    private fun suggestFocalLength(sceneType: com.example.fangcunxu.ai.SceneType): String {
        return when (sceneType) {
            com.example.fangcunxu.ai.SceneType.PORTRAIT -> "50mm - 85mm (人像焦段)"
            com.example.fangcunxu.ai.SceneType.LANDSCAPE -> "24mm - 35mm (广角焦段)"
            com.example.fangcunxu.ai.SceneType.CITY -> "35mm - 50mm (标准焦段)"
            com.example.fangcunxu.ai.SceneType.STREET -> "35mm - 50mm (街拍焦段)"
            com.example.fangcunxu.ai.SceneType.ARCHITECTURE -> "24mm - 35mm (广角拍摄建筑)"
            com.example.fangcunxu.ai.SceneType.MACRO -> "90mm - 105mm (微距焦段)"
            com.example.fangcunxu.ai.SceneType.SPORTS -> "70mm - 200mm (长焦捕捉运动)"
            com.example.fangcunxu.ai.SceneType.FOOD -> "50mm - 85mm (中焦拍摄美食)"
            com.example.fangcunxu.ai.SceneType.STILL_LIFE -> "50mm - 85mm (标准焦段)"
            com.example.fangcunxu.ai.SceneType.NIGHT -> "35mm - 50mm (夜景标准焦段)"
            com.example.fangcunxu.ai.SceneType.ANIMAL -> "70mm - 200mm (长焦拍摄动物)"
            com.example.fangcunxu.ai.SceneType.INDOOR -> "35mm - 50mm (室内标准焦段)"
            com.example.fangcunxu.ai.SceneType.OUTDOOR -> "24mm - 50mm (户外焦段)"
            com.example.fangcunxu.ai.SceneType.GROUP -> "24mm - 35mm (广角拍摄群体)"
            com.example.fangcunxu.ai.SceneType.PET -> "50mm - 85mm (中焦拍摄宠物)"
            com.example.fangcunxu.ai.SceneType.UNKNOWN -> "35mm - 50mm (标准焦段)"
        }
    }
    
    private fun suggestISO(
        score: Int,
        sceneType: com.example.fangcunxu.ai.SceneType
    ): String {
        return when (sceneType) {
            com.example.fangcunxu.ai.SceneType.NIGHT -> {
                when {
                    score >= 80 -> "ISO 800 - 1600 (高感光夜景)"
                    score >= 60 -> "ISO 1600 - 3200 (超高感光)"
                    else -> "ISO 3200 - 6400 (最大感光)"
                }
            }
            com.example.fangcunxu.ai.SceneType.STREET -> {
                when {
                    score >= 70 -> "ISO 400 - 800 (中感光)"
                    else -> "ISO 800 - 1600 (高感光)"
                }
            }
            com.example.fangcunxu.ai.SceneType.STILL_LIFE -> "ISO 400 - 800 (室内拍摄)"
            com.example.fangcunxu.ai.SceneType.SPORTS -> {
                when {
                    score >= 70 -> "ISO 800 - 1600 (高速快门需要高感光)"
                    else -> "ISO 1600 - 3200 (超高感光)"
                }
            }
            else -> {
                when {
                    score >= 80 -> "ISO 100 - 200 (低感光保证画质)"
                    score >= 60 -> "ISO 200 - 400 (中等感光)"
                    else -> "ISO 400 - 800 (适当感光)"
                }
            }
        }
    }
    
    private fun suggestShutterSpeed(
        score: Int,
        sceneType: com.example.fangcunxu.ai.SceneType
    ): String {
        return when (sceneType) {
            com.example.fangcunxu.ai.SceneType.SPORTS -> {
                when {
                    score >= 70 -> "1/1000 - 1/2000 (超高速冻结动作)"
                    score >= 60 -> "1/500 - 1/1000 (高速快门)"
                    else -> "1/250 - 1/500 (中高速快门)"
                }
            }
            com.example.fangcunxu.ai.SceneType.STREET -> {
                when {
                    score >= 70 -> "1/250 - 1/500 (捕捉动态)"
                    else -> "1/125 - 1/250 (标准快门)"
                }
            }
            com.example.fangcunxu.ai.SceneType.NIGHT -> {
                when {
                    score >= 70 -> "1/60 - 1/125 (慢速快门)"
                    else -> "1/30 - 1/60 (慢快门收集光线)"
                }
            }
            com.example.fangcunxu.ai.SceneType.PORTRAIT -> {
                when {
                    score >= 80 -> "1/125 - 1/250 (标准快门)"
                    else -> "1/60 - 1/125 (慢快门)"
                }
            }
            else -> {
                when {
                    score >= 80 -> "1/250 - 1/500 (标准快门)"
                    score >= 60 -> "1/125 - 1/250 (中等快门)"
                    else -> "1/60 - 1/125 (慢快门)"
                }
            }
        }
    }
    
    private fun generateExplanation(
        score: Int,
        sceneType: com.example.fangcunxu.ai.SceneType
    ): String {
        val scoreLevel = when {
            score >= 90 -> "优秀"
            score >= 75 -> "良好"
            score >= 60 -> "一般"
            else -> "需改进"
        }
        
        return "当前构图评分：$scoreLevel ($score 分)。" +
                "根据${sceneType.displayName}场景特点，" +
                "建议使用上述参数以获得最佳拍摄效果。"
    }
}