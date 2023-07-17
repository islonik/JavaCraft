package my.javacraft.ses.simulator.services;

import my.javacraft.ses.simulator.model.FinanceCode;
import my.javacraft.ses.simulator.db.FinanceDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by nikilipa on 7/23/16.
 */
@Service
public class FinanceService {

    @Autowired
    private FinanceDao dao;

    public void setDao(FinanceDao dao) {
        this.dao = dao;
    }

    public boolean isEnoughMoney(String financeCode, int sum) {
        FinanceCode code = dao.findFinanceCodeByName(financeCode);
        if (code.getDays() - sum >= 0) {
            return true;
        }
        return false;
    }

    public boolean updateFinance(String financeCode, int sum) {
        FinanceCode code = dao.findFinanceCodeByName(financeCode);
        if (code.getDays() - sum >= 0) {
            return dao.updateFinanceCode(financeCode, code.getDays() - sum);
        }
        return false;
    }

}
