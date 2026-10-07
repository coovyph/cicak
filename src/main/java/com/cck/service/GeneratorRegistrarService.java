package com.cck.service;

import com.cck.component.field.IFieldValueGenerator;

public interface GeneratorRegistrarService {
  public IFieldValueGenerator getFieldValueGenerator(String name);
}
