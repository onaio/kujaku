package io.ona.kujaku.sample;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.ona.kujaku.domain.PointModel;
import io.ona.kujaku.sample.repository.PointsRepository;

import static io.ona.kujaku.sample.util.Constants.DATABASE_NAME;
import static junit.framework.Assert.assertEquals;

/**
 * @author Vincent Karuri
 */
public class PointsRepositoryTest extends BaseRepositoryTest {

    private PointsRepository database;

    @Before
    public void setUp() {
        context.deleteDatabase(DATABASE_NAME);
        database = new PointsRepository(mainRepository);
    }

    @After
    public void tearDown() {
        mainRepository.close();
    }

    @Test
    public void testAddOrUpdateShouldAddNewPoint() {
        PointModel pointModel = new PointModel(null, 1, 3);
        database.addOrUpdate(pointModel);
        assertEquals(1, database.getAllPoints().size());
    }

    @Test
    public void testGetAllPointsShouldGetAllAddedPoints() {
        PointModel pointModel = new PointModel(null, 1, 3);
        database.addOrUpdate(pointModel);
        pointModel = new PointModel(null, 4, 7);
        database.addOrUpdate(pointModel);
        pointModel = new PointModel(null, 9, 10);
        database.addOrUpdate(pointModel);
        assertEquals(3, database.getAllPoints().size());
    }

    @Test
    public void testGetPointByIdShouldGetAddedPointById() {
        PointModel expectedPointModel = new PointModel(null, 1, 3);
        database.addOrUpdate(expectedPointModel);

        PointModel pointModel = new PointModel(null, 4, 7);
        database.addOrUpdate(pointModel);
        pointModel = new PointModel(null, 9, 10);
        database.addOrUpdate(pointModel);

        PointModel actualPointModel = database.getPoint("1");
        assertEquals((int) actualPointModel.getId(), 1);
        assertEquals(actualPointModel.getLat(), expectedPointModel.getLat());
        assertEquals(actualPointModel.getLng(), expectedPointModel.getLng());
    }
}
