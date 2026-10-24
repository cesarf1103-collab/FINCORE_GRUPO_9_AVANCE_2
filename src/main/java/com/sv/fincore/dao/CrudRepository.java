package com.sv.fincore.dao;

/** Contrato CRUD para entidades que admiten modificación y eliminación. */
public interface CrudRepository<T, ID> extends ReadRepository<T, ID>, WriteRepository<T>, DeletableRepository<ID> {
}
