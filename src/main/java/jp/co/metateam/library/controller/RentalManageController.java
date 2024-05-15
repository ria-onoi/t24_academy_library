package jp.co.metateam.library.controller;
 
//import java.net.URI;
//import java.nio.charset.StandardCharsets;
//import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
//import org.springframework.web.util.UriComponentsBuilder;
//import org.springframework.web.util.UriUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;

import jp.co.metateam.library.service.AccountService;
import jp.co.metateam.library.service.RentalManageService;
import jp.co.metateam.library.service.StockService; 
//import jp.co.metateam.library.service.BookMstService;
import jp.co.metateam.library.model.RentalManage;
import jp.co.metateam.library.model.RentalManageDto;
import jp.co.metateam.library.model.Account;
import jp.co.metateam.library.model.Stock;
//import jp.co.metateam.library.model.StockDto;
//import jp.co.metateam.library.model.BookMst;
//import jp.co.metateam.library.model.BookMstDto;
//import jp.co.metateam.library.model.StockDto;
//import jp.co.metateam.library.values.StockStatus;
import jp.co.metateam.library.values.RentalStatus;

import lombok.extern.log4j.Log4j2;

import jakarta.validation.Valid;


/**
 * 貸出管理関連クラスß
 */
@Log4j2
@Controller
public class RentalManageController {
 
    private final AccountService accountService;
    private final RentalManageService rentalManageService;
    private final StockService stockService;
 
    @Autowired
    public RentalManageController(
        AccountService accountService,
        RentalManageService rentalManageService,
        StockService stockService
    ) {
        this.accountService = accountService;
        this.rentalManageService = rentalManageService;
        this.stockService = stockService;
    }
 
    /**
     * 貸出一覧画面初期表示
     * @param model
     * @return
     */
    @GetMapping("/rental/index")
    public String index(Model model) {
        // 貸出管理テーブルから全件取得
 
        List<RentalManage> RentalManageList = this.rentalManageService.findAll();
           
   
        // 貸出一覧画面に渡すデータをmodelに追加
 
        model.addAttribute("rentalManageList", RentalManageList);
 
        // 貸出一覧画面に遷移
 
        return "rental/index";
    }

    @GetMapping("/rental/add")
    public String add(Model model) {
 
        List <Stock> stockList = this.stockService.findAll();
        List <Account> accounts = this.accountService.findAll();
 
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
 
        if (!model.containsAttribute("rentalManageDto")) {
            model.addAttribute("rentalManageDto", new RentalManageDto());
        }
 
        return "rental/add";
    }
    
    @PostMapping("/rental/add")
    public String save(@Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        try {
            if (result.hasErrors()) {
                throw new Exception("Validation error.");
            }
            // 登録処理
            this.rentalManageService.save(rentalManageDto);
 
            return "redirect:/rental/index";
        } catch (Exception e) {
            log.error(e.getMessage());
 
            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);
 
            return "redirect:/rental/add";
        }
    }  

    @GetMapping("/rental/{id}/edit")
    public String edit(@PathVariable("id") String id, Model model) {

        //アカウントテーブルから全件取得
        //在庫管理テーブルから全件取得
        List <Account> accounts = this.accountService.findAll();
        List <Stock> stockList = this.stockService.findAll();

        // 貸出編集画面に渡すデータをmodelに追加 
        //プルダウンのリストに表示するデータをセット
        model.addAttribute("accounts", accounts);
        model.addAttribute("stockList", stockList);
        model.addAttribute("rentalStatus", RentalStatus.values());
        
      
        //初期表示用のデータをセット
        if (!model.containsAttribute("rentalManageDto")) {
            RentalManageDto rentalManageDto = new RentalManageDto();
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            rentalManageDto.setId(rentalManage.getId());
            rentalManageDto.setStockId(rentalManage.getStock().getId());
            rentalManageDto.setExpectedRentalOn(rentalManage.getExpectedRentalOn());
            rentalManageDto.setExpectedReturnOn(rentalManage.getExpectedReturnOn());
            rentalManageDto.setStatus(rentalManage.getStatus());
            rentalManageDto.setEmployeeId(rentalManage.getAccount().getEmployeeId());

            model.addAttribute("rentalManageDto", rentalManageDto);
        }

        return "rental/edit";
    }
 
    @PostMapping("/rental/{id}/edit")
    public String update(@PathVariable("id") String id, @Valid @ModelAttribute RentalManageDto rentalManageDto, BindingResult result, RedirectAttributes ra) {
        //エラーが出た際の処理をtry-catch文で書く
        //tryでエラーが出る可能性のある処理
        try {
            /*throw はjava的には問題がなくコンパイル時も実行時もエラーがでない
             * けど、人間的にエラーとしたいといったときにわざとエラーを出させる
             * if文に引っかかる時は、エラーが出るようにしている
             * .hasErrorsはSpringBoot
             */
            if (result.hasErrors()) {
                //throwでエラーを出させるようにしている
                throw new Exception("Validation error.");
            }

            /*①バリデーションを行いたいデータを準備
             *②バリデーションメソッドを呼び出し、結果を取得
             *③バリデーション結果を処理
             */

            //①で変更前のステータスを取得
            RentalManage rentalManage = this.rentalManageService.findById(Long.valueOf(id));
            Integer previousRentalStatus = rentalManage.getStatus();
            
        

            //②デバリデーションの呼出し
            Optional<String> validationResult = rentalManageDto.validationStatus(previousRentalStatus);
            if (validationResult.isPresent()) {
                // ③：エラーメッセージを処理する
                result.addError(new FieldError("rentalManageDto", "status", validationResult.get()));
                throw new Exception("Validation error.");
            } 

            Optional<String> validationRentalResult = rentalManageDto.validationRentalOn();
            if (validationRentalResult.isPresent()) {
                // ③：エラーメッセージを処理する
                result.addError(new FieldError("rentalManageDto", "expectedRentalOn", validationRentalResult.get()));
                throw new Exception("Validation error.");
            } 
               
            // ③：バリデーションが成功した場合の処理
            // つまり、更新処理
            this.rentalManageService.update(Long.valueOf(id), rentalManageDto);

            return "redirect:/rental/index";
            


            
        //catchでエラーが出た際の処理
        } catch (Exception e) {
            log.error(e.getMessage());

            ra.addFlashAttribute("rentalManageDto", rentalManageDto);
            ra.addFlashAttribute("org.springframework.validation.BindingResult.rentalManageDto", result);

        

            return String.format("redirect:/rental/%s/edit", id);

        }
    }

    
 
}