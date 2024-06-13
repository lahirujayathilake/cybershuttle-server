package org.apache.cybershuttle.model.application;

import java.util.function.Supplier;

public enum ApplicationType {
    VMD(VMDGenerator::new),
    JUPYTER_LAB(JLGenerator::new);

    private final Supplier<? extends ExperimentGenerator> generatorSupplier;

    ApplicationType(Supplier<? extends ExperimentGenerator> generatorSupplier) {
        this.generatorSupplier = generatorSupplier;
    }

    public Supplier<? extends ExperimentGenerator> getGeneratorSupplier() {
        return generatorSupplier;
    }
}