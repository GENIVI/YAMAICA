/* Copyright (C) 2013-2015 BMW Group
 * Author: Manfred Bathelt (manfred.bathelt@bmw.de)
 * Author: Juergen Gehring (juergen.gehring@bmw.de)
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/. */
package de.bmw.yamaica.franca.ui;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;

import de.bmw.yamaica.common.ui.dialogs.YamaicaWizardNewFilePage;
import de.bmw.yamaica.franca.common.core.FrancaUtils;

public class InterfaceDefinitionNewPage extends YamaicaWizardNewFilePage implements ISelectionChangedListener
{
    private static final String THE_DECLARED_PACKAGE_NAME_DOES_NOT_MATCH_TO_THE_FILE_PATH = "The declared package name does not match to the file path.";
    private static final String INVALID_PACKAGE_DECLARATION = "Invalid package declaration. ";
    private static final String INVALID_PACKAGE_NAME = "Invalid package name.";
    private static final String ORG_EXAMPLE = "org.example";
    private static final String PACKAGE = "&Package:";
    private static final String PACKAGE_SUGGESTIONS = "Package &suggestions:";
    private static final String CREATE_A_NEW_FRANCA_INTERFACE_DEFINITION_FILE = "Create a new Franca interface definition file.";
    private static final String FRANCA_INTERFACE_DEFINITION = "Franca Interface Definition";
    private static final String NEW_FRANCA_INTERFACE_DEFINITION = "New Franca Interface Definition";
    protected TableViewer packageSuggestionsTableViewer = null;
    protected Text        packageText                   = null;
    protected IPath       packagePath                   = new Path("");

    public InterfaceDefinitionNewPage(IWorkbench workbench, IStructuredSelection structuredSelection)
    {
        super(workbench, structuredSelection, NEW_FRANCA_INTERFACE_DEFINITION);

        setTitle(FRANCA_INTERFACE_DEFINITION);
        setDescription(CREATE_A_NEW_FRANCA_INTERFACE_DEFINITION_FILE);
    }

    @Override
    protected String[] getFileExtensions()
    {
        return new String[] { FrancaUtils.INTERFACE_DEFINITION_FILE_EXTENSION };
    }

    @Override
    protected void extendFilenameGroup(Composite parent)
    {
        parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        Font font = parent.getFont();

        Label packageSuggestionLabel = new Label(parent, SWT.NONE);
        packageSuggestionLabel.setText(PACKAGE_SUGGESTIONS);
        packageSuggestionLabel.setLayoutData(new GridData(SWT.LEAD, SWT.TOP, false, false));
        packageSuggestionLabel.setFont(font);

        GridData packageSuggestionsTableGridData = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
        packageSuggestionsTableGridData.heightHint = 140;

        Table packageSuggestionsTable = new Table(parent, SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION);
        packageSuggestionsTable.setLayoutData(packageSuggestionsTableGridData);
        packageSuggestionsTable.setFont(font);
        packageSuggestionsTable.addControlListener(new ControlAdapter()
        {
            @Override
            public void controlResized(ControlEvent e)
            {
                Table table = (Table) e.widget;
                table.getColumn(0).setWidth(table.getClientArea().width);
            }
        });

        packageSuggestionsTableViewer = new TableViewer(packageSuggestionsTable);
        packageSuggestionsTableViewer.setContentProvider(new FrancaPackageContentProvider());
        packageSuggestionsTableViewer.addSelectionChangedListener(this);

        TableViewerColumn packageNameViewerColumn = new TableViewerColumn(packageSuggestionsTableViewer, SWT.LEAD);
        packageNameViewerColumn.setLabelProvider(new FrancaPackageLabelProvider());

        Label packageLabel = new Label(parent, SWT.NONE);
        packageLabel.setText(PACKAGE);
        packageLabel.setLayoutData(new GridData(SWT.LEAD, SWT.CENTER, false, false));
        packageLabel.setFont(font);

        packageText = new Text(parent, SWT.SINGLE | SWT.BORDER);
        packageText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
        packageText.addListener(SWT.Modify, this);
        packageText.setMessage(ORG_EXAMPLE);
        packageText.setFont(font);
    }

    @Override
    protected boolean validateExtensions()
    {
        if (packagePath.segmentCount() == 0)
        {
            setErrorMessage(INVALID_PACKAGE_NAME);

            return false;
        }

        try
        {
            if (null != packageText)
            {
                FrancaUtils.normalizeNamespaceString(packageText.getText(), FrancaUtils.NONE, FrancaUtils.DEPLOYMENT_DEFINITION_KEYWORDS);
            }
        }
        catch (IllegalArgumentException e)
        {
            setErrorMessage(INVALID_PACKAGE_DECLARATION + e.getMessage());

            return false;
        }

        IPath filePath = getFileFullPath().removeFileExtension().makeRelative();
        int segmentDelta = filePath.segmentCount() - packagePath.segmentCount();

        if (segmentDelta > 0)
        {
            filePath = filePath.removeFirstSegments(segmentDelta);
        }

        if (!filePath.equals(packagePath))
        {
            setMessage(THE_DECLARED_PACKAGE_NAME_DOES_NOT_MATCH_TO_THE_FILE_PATH, WARNING);
        }
        else
        {
            setMessage(null, WARNING);
        }

        return true;
    }

    @Override
    public void handleEvent(Event event)
    {
        super.handleEvent(event);

        if ((event.widget == packageText || event.widget == targetFolderText || event.widget == filenameText) && null != packageText)
        {
            String packagePathAsString = packageText.getText();
            packagePath = new Path(FrancaUtils.namespace2PathString(packagePathAsString));

            IStructuredSelection structuredSelection = new StructuredSelection(new FrancaPackagePathContainer(packagePathAsString, false));

            packageSuggestionsTableViewer.setSelection(structuredSelection, true);

            updateWidgetEnablements();
        }
    }

    @Override
    protected void updateWidgetEnablements()
    {
        super.updateWidgetEnablements();

        if (null != packageSuggestionsTableViewer)
        {
            if (validateFilenameGroup())
            {
                packageSuggestionsTableViewer.setInput(getFileFullPath());
            }
            else
            {
                packageSuggestionsTableViewer.setInput(null);
            }
        }
    }

    @Override
    public void selectionChanged(SelectionChangedEvent event)
    {
        Table table = ((TableViewer) event.getSource()).getTable();
        TableItem[] tableItems = table.getSelection();

        if (tableItems.length > 0)
        {
            String packagePathAsString = tableItems[0].getText();

            if (!packagePathAsString.equals(packageText.getText()))
            {
                packageText.setText(packagePathAsString);
            }
        }
    }

    @Override
    protected IRunnableWithProgress getFileCreator()
    {
        String packageString = packageText.getText();
        boolean addDemoContent = demoContentCheckbox.getSelection();

        return new InterfaceDefinitionCreationOperation(getSpecifiedFile(), packageString, addDemoContent);
    }
}
