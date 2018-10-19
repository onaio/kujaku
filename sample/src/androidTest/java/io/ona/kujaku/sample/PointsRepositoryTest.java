package io.ona.kujaku.sample;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.ona.kujaku.sample.domain.Point;
import io.ona.kujaku.sample.repository.PointsRepository;

import static io.ona.kujaku.sample.repository.PointsRepository.POINTS_TABLE;
import static io.ona.kujaku.utils.Constants.DATABASE_NAME;
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
        Point point = new Point(null, 1, 3);
        database.addOrUpdate(point);
        assertEquals(1, database.getAllPoints().size());
    }

    @Test
    public void testGetAllPointsShouldGetAllAddedPoints() {
        Point point = new Point(null, 1, 3);
        database.addOrUpdate(point);
        point = new Point(null, 4, 7);
        database.addOrUpdate(point);
        point = new Point(null, 9, 10);
        database.addOrUpdate(point);
        assertEquals(3, database.getAllPoints().size());
    }

    @Test
    public void testGetPointByIdShouldGetAddedPointById() {
        Point expectedPoint = new Point(null, 1, 3);
        database.addOrUpdate(expectedPoint);

        Point point = new Point(null, 4, 7);
        database.addOrUpdate(point);
        point = new Point(null, 9, 10);
        database.addOrUpdate(point);

        Point actualPoint  = database.getPoint("1");
        assertEquals((int) actualPoint.getId(), 1);
        assertEquals(actualPoint.getLat(), expectedPoint.getLat());
        assertEquals(actualPoint.getLng(), expectedPoint.getLng());
    }
}
