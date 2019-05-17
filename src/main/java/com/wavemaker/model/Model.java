package com.wavemaker.model;

import java.util.List;

public class Model {

    private String modelName;

    private List<ModelData> modelDataList;

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public List<ModelData> getModelDataList() {
        return modelDataList;
    }

    public void setModelDataList(List<ModelData> modelDataList) {
        this.modelDataList = modelDataList;
    }
}
