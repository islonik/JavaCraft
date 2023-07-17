package my.javacraft.ses.simulator.services;

import my.javacraft.ses.simulator.db.FinanceDao;
import org.junit.Assert;
import org.junit.runner.RunWith;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by nikilipa on 7/25/16.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/application.xml" })
public class FinanceServiceTest {

    @Autowired
    private FinanceService financeService;

    @Test
    public void testIsEnoughMoney() {
        String financeCode = FinanceDao.FINANCE_CODE_MIGRATION;

        int days1 = 40;
        int days2 = 60;
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days1));
        Assert.assertTrue(financeService.updateFinance(financeCode, days1));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days2));
        Assert.assertTrue(financeService.updateFinance(financeCode, days2));

        int days3 = 80;
        int days4 = 20;
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days3));
        Assert.assertTrue(financeService.updateFinance(financeCode, days3));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days4));
        Assert.assertTrue(financeService.updateFinance(financeCode, days4));

        int days5 = 50;
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days5));
        Assert.assertTrue(financeService.updateFinance(financeCode, days5));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days5));
        Assert.assertTrue(financeService.updateFinance(financeCode, days5));

        int days6 = 30;
        int days7 = 70;
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days6));
        Assert.assertTrue(financeService.updateFinance(financeCode, days6));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days7));
        Assert.assertTrue(financeService.updateFinance(financeCode, days7));

        int days8 = 10;
        int days9 = 85;
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days8));
        Assert.assertTrue(financeService.updateFinance(financeCode, days8));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days9));
        Assert.assertTrue(financeService.updateFinance(financeCode, days9));

        int days10 = 5;
        Assert.assertFalse(financeService.isEnoughMoney(financeCode, days8));
        Assert.assertFalse(financeService.updateFinance(financeCode, days8));
        Assert.assertTrue(financeService.isEnoughMoney(financeCode, days10));
        Assert.assertTrue(financeService.updateFinance(financeCode, days10));
    }
}
