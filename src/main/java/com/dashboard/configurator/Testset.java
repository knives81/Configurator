package com.dashboard.configurator;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

@AllArgsConstructor
@ToString
public class Testset {
    @Getter Integer id;
    @Getter String tag1;
    @Getter String tag2;
    @Getter String prj;
}
