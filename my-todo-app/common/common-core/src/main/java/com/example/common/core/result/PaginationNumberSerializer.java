package com.example.common.core.result;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * 分页数字序列化器
 * <p>
 * 专门用于分页相关的数字字段序列化，确保这些字段始终输出为数字类型，
 * 不受全局 Long->String 配置的影响。
 * </p>
 * <p>
 * 使用场景：PageResult 的 total, current, size, pages 字段
 * </p>
 */
public class PaginationNumberSerializer extends JsonSerializer<Long> {

    @Override
    public void serialize(Long value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        if (value == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(value);
        }
    }
}
