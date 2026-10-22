package com.sv.fincore.dao;

/** Full CRUD contract for mutable entities. Append-only records must not implement this interface. */
public interface CrudRepository<T, ID> extends ReadRepository<T, ID>, WriteRepository<T>, DeletableRepository<ID> {
}
