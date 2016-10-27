/*******************************************************************************
 * QBiC Project Wizard enables users to create hierarchical experiments including different study
 * conditions using factorial design. Copyright (C) "2016" Andreas Friedrich
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If
 * not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package uicomponents;

import io.DBVocabularies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import main.ProjectwizardUI;

import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.server.FontAwesome;
import com.vaadin.shared.ui.combobox.FilteringMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;

import control.Functions;
import control.Functions.NotificationType;

import com.vaadin.ui.ComboBox;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

public class MSPanel extends VerticalLayout {

  /**
   * 
   */
  private static final long serialVersionUID = -2282545855402710972L;
  // private ComboBox msProtocol;
  private TextField lcName;
  private ComboBox lcmsMethod;
  private TextArea lcmsSpecial;
  private TextArea msInfo;
  private ComboBox chromType;
  private ComboBox device;
  private List<String> enzymes;
  private List<EnzymeChooser> choosers;
  private Button.ClickListener buttonListener;
  private VerticalLayout enzymePane;
  private GridLayout buttonGrid;
  private Button add;
  private Button remove;
  DBVocabularies vocabs;

  OptionGroup conditionsSet;

  public MSPanel(DBVocabularies vocabs, OptionGroup conditionsSet) {
    this.setCaption("Mass Spectrometry");
    this.vocabs = vocabs;
    this.enzymes = vocabs.getEnzymes();

    this.conditionsSet = conditionsSet;
    this.conditionsSet.addItem("set");
    add = new Button();
    remove = new Button();
    ProjectwizardUI.iconButton(add, FontAwesome.PLUS_SQUARE);
    ProjectwizardUI.iconButton(remove, FontAwesome.MINUS_SQUARE);
    initListener();

    choosers = new ArrayList<EnzymeChooser>();
    EnzymeChooser c = new EnzymeChooser(enzymes);
    choosers.add(c);
    // addComponent(c);
    // msProtocol = new ComboBox("MS Protocol", vocabs.getMsProtocols());
    // msProtocol.setNullSelectionAllowed(false);
    // msProtocol.setStyleName(ProjectwizardUI.boxTheme);
    device = new ComboBox("MS Device", vocabs.getDeviceMap().keySet());
    device.setFilteringMode(FilteringMode.CONTAINS);
    // device.setNullSelectionAllowed(false);
    device.setStyleName(ProjectwizardUI.boxTheme);
    device.setWidth("300px");
    chromType = new ComboBox("MS Chromatography Type", vocabs.getChromTypes());
    chromType.setFilteringMode(FilteringMode.CONTAINS);
    chromType.setStyleName(ProjectwizardUI.boxTheme);
    lcName = new TextField("LC Name");
    lcName.setStyleName(ProjectwizardUI.fieldTheme);
    lcmsMethod = new ComboBox("MS LCMS Method", vocabs.getLcmsMethods());
    lcmsMethod.setFilteringMode(FilteringMode.CONTAINS);
    lcmsMethod.setStyleName(ProjectwizardUI.boxTheme);
    lcmsMethod.setWidth("350px");
    lcmsSpecial = new TextArea("LCMS Method Name");
    lcmsSpecial.setStyleName(ProjectwizardUI.areaTheme);
    lcmsSpecial.setVisible(false);
    msInfo = new TextArea("Additional Notes");
    msInfo.setStyleName(ProjectwizardUI.areaTheme);
    // addComponent(ProjectwizardUI.questionize(msProtocol,
    // "Designates the general MS protocol for this measurement (e.g. targeted metabolomics)",
    // "MS Protocol"));
    addComponent(ProjectwizardUI.questionize(device,
        "The MS device that is used to conduct the experiment.", "MS Device"));
    addComponent(ProjectwizardUI.questionize(chromType,
        "Specifies the kind of chromatography that is coupled to the mass spectrometer.",
        "Chromatography Type"));
    addComponent(ProjectwizardUI.questionize(lcName, "Name of the Column", "LC Name"));
    addComponent(ProjectwizardUI.questionize(lcmsMethod,
        "Labratory specific parameters for LCMS measurements.", "LCMS Method"));
    addComponent(lcmsSpecial);
    addComponent(msInfo);

    lcmsMethod.addValueChangeListener(new ValueChangeListener() {

      @Override
      public void valueChange(ValueChangeEvent event) {
        String val = (String) lcmsMethod.getValue();
        boolean special = val.equals("SPECIAL_METHOD");
        lcmsSpecial.setVisible(special);
        if (!special)
          lcmsSpecial.setValue("");
      }
    });

    enzymePane = new VerticalLayout();
    enzymePane.setCaption("Digestion Enzymes");
    enzymePane.addComponent(c);
    addComponent(enzymePane);
    buttonGrid = new GridLayout(2, 1);
    buttonGrid.setSpacing(true);
    buttonGrid.addComponent(add);
    buttonGrid.addComponent(remove);
    addComponent(buttonGrid);
    setSpacing(true);
  }

  private void initListener() {
    buttonListener = new Button.ClickListener() {

      private static final long serialVersionUID = 2240224129259577437L;

      @Override
      public void buttonClick(ClickEvent event) {
        if (event.getButton().equals(add))
          add();
        else
          remove();
      }
    };
    add.addClickListener(buttonListener);
    remove.addClickListener(buttonListener);
  }

  public Map<String, Object> getExperimentalProperties() {
    Map<String, Object> res = new HashMap<String, Object>();
    res.put("Q_MS_DEVICE", vocabs.getDeviceMap().get(device.getValue()));
    res.put("Q_MS_LCMS_METHOD", lcmsMethod.getValue());
    res.put("Q_CHROMATOGRAPHY_TYPE", chromType.getValue());
    res.put("Q_CHROMATOGRAPHY_COLUMN_NAME", lcName.getValue());
    res.put("Q_MS_LCMS_METHOD_INFO", lcmsSpecial.getValue());
    res.put("Q_ADDITIONAL_INFO", msInfo.getValue());
    List<String> remove = new ArrayList<String>();
    for (String key : res.keySet()) {
      String val = (String) res.get(key);
      if (val == null || val.isEmpty())
        remove.add(key);
    }
    for (String key : remove)
      res.remove(key);
    res.put("ENZYMES", getEnzymes());
    return res;
  }

  public List<String> getEnzymes() {
    List<String> res = new ArrayList<String>();
    for (EnzymeChooser c : choosers) {
      if (c.isSet())
        res.add(c.getEnzyme());
    }
    return res;
  }

  private void add() {
    if (choosers.size() < 4) {
      EnzymeChooser c = new EnzymeChooser(enzymes);
      choosers.add(c);

      removeComponent(buttonGrid);
      enzymePane.addComponent(c);
      addComponent(buttonGrid);
    }
  }

  private void remove() {
    int size = choosers.size();
    if (size > 1) {
      EnzymeChooser last = choosers.get(size - 1);
      last.reset();
      enzymePane.removeComponent(last);
      choosers.remove(last);
    }
  }

  public boolean isValid() {
    // TODO restrictions?
    // Set<String> uniques = new HashSet<String>();
    // boolean nonEmpty = false;
    // for (EnzymeChooser c : choosers) {
    // uniques.add(c.getEnzyme());
    // nonEmpty |= (!(c.getEnzyme() == null) && !c.getEnzyme().isEmpty());
    // }
    // if (uniques.size() < choosers.size() || !nonEmpty) {
    // Functions.notification("Wrong input", "Please input at least one enzyme and the same enzyme
    // only once.", NotificationType.ERROR);
    // return false;
    // } else
    return true;
  }

  public void resetInputs() {
    for (EnzymeChooser c : choosers) {
      c.reset();
    }
  }

}
