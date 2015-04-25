/*
 * Copyright 2013-2014 Sergey Ignatov, Alexander Zolotov
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

package com.goide.inspections.unresolved;

import com.goide.inspections.GoInspectionBase;
import com.goide.psi.*;
import com.intellij.codeInspection.LocalInspectionToolSession;
import com.intellij.codeInspection.LocalQuickFixOnPsiElement;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.Query;
import org.jetbrains.annotations.NotNull;

public class GoUnusedVariableInspection extends GoInspectionBase {
  @NotNull
  @Override
  protected GoVisitor buildGoVisitor(@NotNull final ProblemsHolder holder,
                                     @SuppressWarnings({"UnusedParameters", "For future"}) @NotNull LocalInspectionToolSession session) {
    return new GoVisitor() {
      @Override
      public void visitVarDefinition(@NotNull GoVarDefinition o) {
        if (o.isBlank()) return;
        GoVarSpec varSpec = PsiTreeUtil.getParentOfType(o, GoVarSpec.class);
        GoVarDeclaration decl = PsiTreeUtil.getParentOfType(o, GoVarDeclaration.class);
        if (varSpec != null || decl != null) {
          PsiReference reference = o.getReference();
          PsiElement resolve = reference != null ? reference.resolve() : null;
          if (resolve != null) return;
          Query<PsiReference> query = ReferencesSearch.search(o, o.getUseScope());
          boolean shortDecl = PsiTreeUtil.getParentOfType(o, GoShortVarDeclaration.class, GoVarDeclaration.class) instanceof GoShortVarDeclaration;
          for (PsiReference ref : query) {
            PsiElement element = ref.getElement();
            if (element == null) continue;
            PsiElement parent = element.getParent();
            if (shortDecl && parent instanceof GoLeftHandExprList && parent.getParent() instanceof GoAssignmentStatement) continue;
            if (parent instanceof GoShortVarDeclaration) {
              int op = ((GoShortVarDeclaration)parent).getVarAssign().getStartOffsetInParent();
              if (element.getStartOffsetInParent() < op) continue;
            }
            return;
          }
          boolean globalVar = decl != null && decl.getParent() instanceof GoFile;
          if (globalVar) {
            if (!checkGlobal()) return;
            holder.registerProblem(o, "Unused variable " + "'" + o.getText() + "'", ProblemHighlightType.LIKE_UNUSED_SYMBOL);
          }
          else {
            if (checkGlobal()) return;
            holder.registerProblem(o, "Unused variable " + "'" + o.getText() + "'", ProblemHighlightType.GENERIC_ERROR_OR_WARNING,
                                   new LocalQuickFixOnPsiElement(o) {
                                     @NotNull
                                     @Override
                                     public String getText() {
                                       return "Rename to _";
                                     }

                                     @Override
                                     public void invoke(@NotNull Project project,
                                                        @NotNull PsiFile file,
                                                        @NotNull PsiElement element,
                                                        @NotNull PsiElement element1) {
                                       if (element instanceof GoVarDefinition) {
                                         ((GoVarDefinition)element).setName("_");
                                       }
                                     }

                                     @NotNull
                                     @Override
                                     public String getFamilyName() {
                                       return "Go";
                                     }
                                   });
          }
        }
      }
    };
  }

  protected boolean checkGlobal() {
    return false;
  }
}
