package com.catchylabs.implementations;

import com.catchylabs.steps.BaseSteps;
import com.thoughtworks.gauge.Step;

public class StepImps {
    private final BaseSteps baseSteps;

    public StepImps() {
        baseSteps = BaseSteps.getInstance();
    }

    @Step("<key> elementine tıklanır.")
    public void click(String keyword) {
        baseSteps.click(keyword);
    }

    @Step("<second> saniye kadar beklenir.")
    public void waitSecond(long second) {
        baseSteps.waitSeconds(second);
    }

    @Step("<miliSeconds> milisaniye kadar beklenir.")
    public void waitMiliseconds(long milis) {
        baseSteps.waitMilliseconds(milis);
    }

    @Step("<key> elementine <text> değeri girilir.")
    public void sendKeys(String keyword, String text) {
        baseSteps.sendKeys(keyword, text);
    }

    @Step("<key> elementinden <text> değeri seçilir.")
    public void selectByText(String keyword, String text) {
        baseSteps.selectByText(keyword, text);
    }

    @Step("<key> elementinin görünürlüğü kontrol edilir.")
    public void isElemetVisible(String keyword) {
        baseSteps.isElementVisible(keyword);
    }
}
