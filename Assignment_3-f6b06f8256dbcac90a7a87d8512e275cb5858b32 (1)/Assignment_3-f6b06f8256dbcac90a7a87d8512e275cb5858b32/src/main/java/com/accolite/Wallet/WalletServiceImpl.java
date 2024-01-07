package com.accolite.Wallet;

import com.accolite.Users.UsersEntity;
import com.accolite.Users.UsersRepository;
import com.accolite.Users.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private UsersRepository usersRepository;
    @Autowired
    private WalletRepository walletRepository;


    @Override
    public List<WalletEntity> getAllWalletDetails() {
        return walletRepository.findAll();
    }

    @Override
    public void saveWallet(WalletEntity wallet, Long id) {
        if (wallet != null) {
            if (id == 3) {
                Long uniqueCode = generateUniqueCode();
                List<Long> unique_code = new ArrayList<>();
                unique_code.add(uniqueCode);
                wallet.setUniqueCodes(unique_code);
            }
            walletRepository.save(wallet);
        }
    }

    @Override
    public ResponseEntity<String> addMoney(Long id, Double amount) {
        WalletEntity wallet = walletRepository.findById(id).orElse(null);
        if (wallet != null) {
            UsersEntity usersEntity = usersRepository.findById(id).orElse(null);
            if (usersEntity.getRole().getId() == 3) {
                if (wallet.getStatus() != 0) {
                    Double newAmount = wallet.getAmount() + amount;
                    wallet.setAmount(newAmount);
                    walletRepository.save(wallet);
                    return new ResponseEntity<>("Amount Added", HttpStatus.CREATED);
                } else
                    return new ResponseEntity<>("Inactive Account", HttpStatus.NOT_MODIFIED);
            } else
                return new ResponseEntity<>("Only User can add Money", HttpStatus.NOT_MODIFIED);
        }

        return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<String> setWalletStatus(Long id) {
        WalletEntity wallet = walletRepository.findById(id).orElse(null);
        if (wallet != null) {
            if (wallet.getStatus() == 1)
                return new ResponseEntity<>("Wallet Activated Already", HttpStatus.NOT_MODIFIED);
            wallet.setStatus((byte) 1);
            walletRepository.save(wallet);
            return new ResponseEntity<>("Wallet Activated", HttpStatus.ACCEPTED);
        }
        return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
    }

    @Override
    public ResponseEntity<String> changePaymentType(Long id) {
        WalletEntity wallet = walletRepository.findById(id).orElse(null);
        if (wallet != null) {
            UsersEntity usersEntity = usersRepository.findById(id).orElse(null);
            if (usersEntity.getRole().getId() == 3) {
                if (wallet.getStatus() != 0 && wallet.getAmount()>=5000) {
                    Byte paymentType = (byte) (wallet.getPaymentType() == 0 ? 1 : 0);
                    if (paymentType == 0) {
                        Long uniqueCode = generateUniqueCode();
                        List<Long> unique_code = new ArrayList<>();
                        unique_code.add(uniqueCode);
                        wallet.setUniqueCodes(unique_code);
                        wallet.setStatus((byte) 0);
                    } else {
                        Set<Long> unique_CodeSet = new HashSet<>();
                        while (true) {
                            if (unique_CodeSet.size() == 5)
                                break;
                            unique_CodeSet.add(generateUniqueCode());
                        }
                        wallet.setUniqueCodes(new ArrayList<>(unique_CodeSet));
                    }
                    wallet.setPaymentType(paymentType);
                    System.out.println(wallet);
                    walletRepository.save(wallet);
                    return new ResponseEntity<>("Payment Updated to " + (paymentType == (byte) 1 ? "Offline" : "Online"), HttpStatus.CREATED);
                } else
                    return new ResponseEntity<>("Wallet Not Activated or Check the Wallet Balance", HttpStatus.NOT_MODIFIED);
            } else
                return new ResponseEntity<>("Only User can change Payment type", HttpStatus.NOT_MODIFIED);

        }
        return new ResponseEntity<>("Not Found", HttpStatus.NOT_FOUND);
    }

    @Override
    public void updateWallet(Long vendorId, Long userId, Double amount) {
        WalletEntity userWallet = walletRepository.findById(userId).get();
        WalletEntity vendorWallet = walletRepository.findById(vendorId).get();
        double userNewAmount = userWallet.getAmount() - amount;
        double vendorNewAmount = vendorWallet.getAmount() + amount;
        userWallet.setAmount(userNewAmount);
        vendorWallet.setAmount(vendorNewAmount);
        walletRepository.save(userWallet);
        walletRepository.save(vendorWallet);
    }

    @Override
    public void updateWallet(Long userId, Double amount) {
        WalletEntity userWallet = walletRepository.findById(userId).get();
        UsersEntity adminEntity = usersRepository.findAll().stream().filter(x->x.getRole().getId()==1).collect(Collectors.toList()).stream().findFirst().get();
        WalletEntity adminWallet = walletRepository.findById(adminEntity.getId()).get();
        double adminNewAmount = adminWallet.getAmount() + amount;
        double userNewAmount = userWallet.getAmount() - amount;
        userWallet.setAmount(userNewAmount);
        adminWallet.setAmount(adminNewAmount);
        walletRepository.save(userWallet);
        walletRepository.save(adminWallet);
    }


    private Long generateUniqueCode() {
        return new Random().nextLong(100000, 300000);
    }
}
