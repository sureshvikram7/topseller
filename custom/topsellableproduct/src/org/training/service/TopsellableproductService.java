/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.training.service;

public interface TopsellableproductService
{
	String getHybrisLogoUrl(String logoCode);

	void createLogo(String logoCode);
}
