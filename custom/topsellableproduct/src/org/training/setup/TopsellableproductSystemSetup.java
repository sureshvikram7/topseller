/*
 * Copyright (c) 2020 SAP SE or an SAP affiliate company. All rights reserved.
 */
package org.training.setup;

import static org.training.constants.TopsellableproductConstants.PLATFORM_LOGO_CODE;

import de.hybris.platform.core.initialization.SystemSetup;

import java.io.InputStream;

import org.training.constants.TopsellableproductConstants;
import org.training.service.TopsellableproductService;


@SystemSetup(extension = TopsellableproductConstants.EXTENSIONNAME)
public class TopsellableproductSystemSetup
{
	private final TopsellableproductService topsellableproductService;

	public TopsellableproductSystemSetup(final TopsellableproductService topsellableproductService)
	{
		this.topsellableproductService = topsellableproductService;
	}

	@SystemSetup(process = SystemSetup.Process.INIT, type = SystemSetup.Type.ESSENTIAL)
	public void createEssentialData()
	{
		topsellableproductService.createLogo(PLATFORM_LOGO_CODE);
	}

	private InputStream getImageStream()
	{
		return TopsellableproductSystemSetup.class.getResourceAsStream("/topsellableproduct/sap-hybris-platform.png");
	}
}
