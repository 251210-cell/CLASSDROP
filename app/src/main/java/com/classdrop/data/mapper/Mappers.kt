package com.classdrop.data.mapper

import com.classdrop.data.local.entity.PrivacyRuleEntity
import com.classdrop.data.local.entity.SubjectEntity
import com.classdrop.model.CommunityRule
import com.classdrop.model.Subject

fun SubjectEntity.toDomain() = Subject(
    id = id,
    name = name,
    fileCount = fileCount,
    iconRes = iconRes,
    iconBgColor = iconBgColor,
    iconTintColor = iconTintColor,
    cuatrimestre = cuatrimestre
)

fun Subject.toEntity() = SubjectEntity(
    id = id,
    name = name,
    fileCount = fileCount,
    iconRes = iconRes,
    iconBgColor = iconBgColor,
    iconTintColor = iconTintColor,
    cuatrimestre = cuatrimestre
)

fun PrivacyRuleEntity.toDomain() = CommunityRule(
    id = id,
    number = number,
    title = title,
    description = description,
    iconResName = iconResName,
    lastEdited = lastEdited,
    isActive = isActive,
    adminNote = adminNote
)

fun CommunityRule.toEntity() = PrivacyRuleEntity(
    id = id,
    number = number,
    title = title,
    description = description,
    iconResName = iconResName,
    lastEdited = lastEdited,
    isActive = isActive,
    adminNote = adminNote
)
