/*
 * Copyright 2013-2016 Sergey Ignatov, Alexander Zolotov, Florin Patan
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.goide.inspections;

import com.goide.inspections.unresolved.GoUnusedLabelInspection;
import com.goide.quickfix.GoQuickFixTestBase;
import com.goide.quickfix.GoRenameToBlankQuickFix;

public class GoUnusedLabelInspectionTest extends GoQuickFixTestBase {
  @Override
  public void setUp() throws Exception {
    super.setUp();
    myFixture.enableInspections(GoUnusedLabelInspection.class);
  }
  
  public void testSimple() {
    myFixture.testHighlighting(getTestName(true) + ".go");
  }
  
  public void testDeleteLabelFix() {
    doTest("Delete label 'UnusedLabel'", true);
  }
  
  public void testRenameToBlankFix() {
    doTest(GoRenameToBlankQuickFix.NAME, true);
  }

  @Override
  protected String getBasePath() {
    return "inspections/unused-label";
  }
}
