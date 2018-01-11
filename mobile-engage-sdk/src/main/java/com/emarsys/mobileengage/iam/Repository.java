package com.emarsys.mobileengage.iam;

import java.util.List;

public interface Repository<T, S> {

    void add(T item);

    void remove(S specification);

    List<T> query(S specification);
}
