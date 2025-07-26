package com.sapuseven.untis.persistence.utils

/**
 * A generic interface for providing a mapping function between two types.
 *
 * Entities in this module use this interface by implementing it in their companion object,
 * allowing you to easily map API objects to the corresponding entity.
 *
 * @param From the source type to map from, i.e. your data model.
 * @param To the target type to map to, i.e. the entity model.
 */
fun interface EntityMapper<in From, out To> {
	/**
	 * Maps an object of type [From] to an object of type [To].
	 *
	 * @param from the source object to map from.
	 * @param userId the user ID associated with the mapping, used to set the user ID in the entity as an additional key.
	 * @return an object of type [To] that represents the mapped entity.
	 */
	fun map(from: From, userId: Long): To
}
