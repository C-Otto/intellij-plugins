/*
 * Copyright (c) 2007-2009, Osmorc Development Team
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright notice, this list
 *       of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice, this
 *       list of conditions and the following disclaimer in the documentation and/or other
 *       materials provided with the distribution.
 *     * Neither the name of 'Osmorc Development Team' nor the names of its contributors may be
 *       used to endorse or promote products derived from this software without specific
 *       prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR
 * TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.osmorc.manifest.lang.headerparser.impl;

import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.psi.PsiReference;
import com.intellij.psi.impl.source.resolve.reference.impl.providers.JavaClassReferenceProvider;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.osmorc.manifest.lang.psi.ManifestClause;
import org.osmorc.manifest.lang.psi.ManifestHeaderValue;
import org.osmorc.manifest.lang.valueparser.ValueParserRepository;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class BundleActivatorParser extends AbstractHeaderParserImpl {
    public BundleActivatorParser(ValueParserRepository valueParserRepository) {
        super(valueParserRepository);
    }

    public PsiReference[] getReferences(@NotNull ManifestHeaderValue headerValue) {
        if (headerValue.getParent() instanceof ManifestClause) {
            Module module = ModuleUtil.findModuleForPsiElement(headerValue);
            JavaClassReferenceProvider provider;
            if (module != null) {
                provider = new JavaClassReferenceProvider(GlobalSearchScope.moduleScope(module), headerValue.getProject());
            } else {
                provider = new JavaClassReferenceProvider(headerValue.getProject());
            }

            provider.setOption(JavaClassReferenceProvider.EXTEND_CLASS_NAMES, new String[]{"org.osgi.framework.BundleActivator"});
            provider.setOption(JavaClassReferenceProvider.CONCRETE, true);
            return provider.getReferencesByElement(headerValue);
        }
        return EMPTY_PSI_REFERENCE_ARRAY;
    }
}