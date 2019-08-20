package org.openecomp.sdcrests.conflict.rest.mapping;

import org.junit.Before;
import org.junit.Test;
import org.openecomp.conflicts.types.ConflictInfo;
import org.openecomp.conflicts.types.ItemVersionConflict;
import org.openecomp.sdc.datatypes.model.ElementType;
import org.openecomp.sdcrests.conflict.types.ConflictInfoDto;
import org.openecomp.sdcrests.conflict.types.ItemVersionConflictDto;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

public class MapItemVersionConflictToDtoTest {

    private MapItemVersionConflictToDto mapItemVersionConflictToDto;


    @Before
    public void setUp() {
        mapItemVersionConflictToDto = new MapItemVersionConflictToDto();
    }

    @Test
    public void validDoMappingCorrectMapsVersionConflicts() {

        final String testId01 = "testId01";
        final String testName01 = "testName01";
        final String testId02 = "testId02";
        final String testName02 = "testName02";

        final ItemVersionConflict testSource = new ItemVersionConflict();
        Collections.addAll(testSource.getElementConflicts(),
                new ConflictInfo(testId01, ElementType.itemVersion, testName01),
                new ConflictInfo(testId02, ElementType.itemVersion, testName02)
        );
        final ItemVersionConflictDto testTarget = new ItemVersionConflictDto();

        mapItemVersionConflictToDto.doMapping(testSource, testTarget);

        assertArrayEquals(
                testSource.getElementConflicts().stream().map(ConflictInfo::getName).toArray(),
                testTarget.getConflictInfoList().stream().map(ConflictInfoDto::getName).toArray()
        );
        assertArrayEquals(
                testSource.getElementConflicts().stream().map(ConflictInfo::getId).toArray(),
                testTarget.getConflictInfoList().stream().map(ConflictInfoDto::getId).toArray()
        );
    }
}
