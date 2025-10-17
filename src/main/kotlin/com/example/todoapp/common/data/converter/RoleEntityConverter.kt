package com.example.todoapp.common.data.converter

import com.example.todoapp.common.data.entity.NewzroomRoleEntity
import org.springframework.core.convert.converter.Converter
import org.springframework.data.convert.ReadingConverter
import org.springframework.data.convert.WritingConverter

@WritingConverter
class RoleEntityToStringConverter : Converter<NewzroomRoleEntity, String> {
	override fun convert(source: NewzroomRoleEntity): String {
		return source.value
	}
}

@ReadingConverter
class StringToRoleEntityConverter : Converter<String, NewzroomRoleEntity> {
	override fun convert(source: String): NewzroomRoleEntity {
		return NewzroomRoleEntity.from(source)
	}
}