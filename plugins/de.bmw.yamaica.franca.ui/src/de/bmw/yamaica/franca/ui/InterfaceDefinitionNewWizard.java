/* Copyright (C) 2013 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.franca.ui;

import de.bmw.yamaica.base.ui.dialogs.YamaicaNewFileWizard;

public class InterfaceDefinitionNewWizard extends YamaicaNewFileWizard
{
    public InterfaceDefinitionNewWizard()
    {
        super("YamaicaFrancaNewInterfaceDefinitionWizard");

        setWindowTitle("New Franca Interface Definition File");
    }

    @Override
    public void addPages()
    {
        yamaicaWizardNewFilePage = new InterfaceDefinitionNewPage(workbench, structuredSelection);

        addPage(yamaicaWizardNewFilePage);
    }
}