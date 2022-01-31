package com.eccenca.braine.dao;

import java.util.Map;

public interface Marshal <O,I> {
	O marshal(I instance, Map<String, String> attrMapping);
}
