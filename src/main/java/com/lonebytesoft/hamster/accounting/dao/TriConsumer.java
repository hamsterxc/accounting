package com.lonebytesoft.hamster.accounting.dao;

@FunctionalInterface
interface TriConsumer<A, B, C> {

    void accept(A a, B b, C c);

}
