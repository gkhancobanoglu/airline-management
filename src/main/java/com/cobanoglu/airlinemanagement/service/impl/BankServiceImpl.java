package com.cobanoglu.airlinemanagement.service.impl;

import com.cobanoglu.airlinemanagement.dto.BankDTO;
import com.cobanoglu.airlinemanagement.entity.Status;
import com.cobanoglu.airlinemanagement.service.BankService;
import org.springframework.stereotype.Service;

@Service
public class BankServiceImpl implements BankService {
    @Override
    public BankDTO process(BankDTO bankDTO) {
        if(bankDTO.getCvv()==999){
            bankDTO.setStatus(Status.Success);
        }else {
            bankDTO.setStatus(Status.Unsuccessful);
        }

        return bankDTO;
    }
}
