package com.example.opensrp_anc.anc;

import org.ei.opensrp.core.template.HeaderProvider;
import org.ei.opensrp.core.template.RegisterClientsProvider;
import org.ei.opensrp.core.template.ServiceModeOption;

/**
 * Created by Owais on 4/4/2017.
 */

public class AncServiceModeOption extends ServiceModeOption {

    private String name;
    private int[] headerTextResourceIds;
    private int[] columnWeights;

    public AncServiceModeOption(RegisterClientsProvider provider, String name, int[] headerTextResourceIds, int[] columnWeights) {
        super(provider);
        this.name = name;
        this.headerTextResourceIds = headerTextResourceIds;
        this.columnWeights = columnWeights;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public HeaderProvider getHeaderProvider() {
        return new HeaderProvider() {
            @Override
            public int count() {
                return headerTextResourceIds.length;
            }

            @Override
            public int weightSum() {
                int sum = 0;
                for (int i = 0; i < columnWeights.length; i++) {
                    sum += columnWeights[i];
                }
                return sum;
            }

            @Override
            public int[] weights() {
                return columnWeights;
            }

            @Override
            public int[] headerTextResourceIds() {
                return headerTextResourceIds;
            }
        };
    }
}
