package com.software.sello.data.mapper

import com.software.sello.data.local.entity.CategoryEntity
import com.software.sello.domain.model.Category
import com.software.sello.domain.model.CategoryId
import com.software.sello.domain.model.CategoryName
import com.software.sello.domain.model.IconKey
import com.software.sello.domain.model.Outcome
import com.software.sello.domain.model.StorageFailure
import java.time.Instant

internal fun Category.toEntity() = CategoryEntity(
    id = id.value,
    name = name.value,
    nameKey = name.uniquenessKey,
    icon = icon.value,
    archived = if (archived) 1 else 0,
    version = version,
    createdAt = createdAt.toEpochMilli(),
    updatedAt = updatedAt.toEpochMilli()
)

internal fun CategoryEntity.toDomain(): Outcome<Category, StorageFailure.Integrity> =
    decodeRow(CategoryEntity.TABLE, id) {
        val name = valid("name", CategoryName.of(this@toDomain.name))
        // The stored text must already be the canonical name and its key, or the unique
        // index has been guarding something other than what is shown.
        check("name", name.value == this@toDomain.name)
        check("name_key", name.uniquenessKey == nameKey)
        check("archived", archived == 0L || archived == 1L)
        check("version", version >= 1)
        Category(
            id = valid("id", CategoryId.of(id)),
            name = name,
            icon = valid("icon", IconKey.of(icon)),
            archived = archived == 1L,
            version = version,
            createdAt = Instant.ofEpochMilli(createdAt),
            updatedAt = Instant.ofEpochMilli(updatedAt)
        )
    }
