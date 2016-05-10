package org.codetwisted.extension;

import android.support.annotation.NonNull;

public class ExtensionBase<T> {

    private final T instanceExtended;


    public ExtensionBase(@NonNull T instanceExtended) {
        this.instanceExtended = instanceExtended;
    }


	@NonNull
    public T getInstanceExtended() {
        return this.instanceExtended;
    }

}
