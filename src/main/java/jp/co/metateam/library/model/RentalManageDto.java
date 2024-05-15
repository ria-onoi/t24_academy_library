package jp.co.metateam.library.model;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jp.co.metateam.library.values.RentalStatus;
import lombok.Getter;
import lombok.Setter;

//追加したインポート
import java.util.Optional;

/**
 * 貸出管理DTO
 */
@Getter
@Setter
public class RentalManageDto {

    private Long id;

    @NotEmpty(message="在庫管理番号は必須です")
    private String stockId;

    @NotEmpty(message="社員番号は必須です")
    private String employeeId;

    @NotNull(message="貸出ステータスは必須です")
    private Integer status;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="貸出予定日は必須です")
    private Date expectedRentalOn;

    @DateTimeFormat(pattern="yyyy-MM-dd")
    @NotNull(message="返却予定日は必須です")
    private Date expectedReturnOn;

    private Timestamp rentaledAt;

    private Timestamp returnedAt;

    private Timestamp canceledAt;

    private Stock stock;

    private Account account;

    //バリデーションチェック
    public Optional<String> validationStatus(Integer previousRentalStatus){
        /*貸出待ち→貸出中かつ貸出予定日が現在の日付になっていない場合のエラー
         *貸出待ち→返却済みの場合のエラー
         */
        //変更前のステータスが貸出待ち　かつ　変更後のステータスが変更されているかどうか
        if (previousRentalStatus == RentalStatus.RENT_WAIT.getValue() && previousRentalStatus != this.status){

            
            
            //変更後のステータスが返却済みの場合
             if(this.status == RentalStatus.RETURNED.getValue()){

                //「「貸出待ち」から「返却済み」にステータス変更できません」という旨のエラー
                return Optional.of("「貸出待ち」から「返却済み」にステータス変更できません");
            }

        }

        /*貸出中→返却済み以外のステータス変更の場合のエラー
         */
        //変更前のステータスが貸出中　かつ　変更後のステータスが変更されているかどうか
        if(previousRentalStatus == RentalStatus.RENTAlING.getValue() && previousRentalStatus != this.status){

            //変更後のステータスが返却済み以外かどうか
            if(this.status != RentalStatus.RETURNED.getValue()){

                //「「貸出中」から「返却済み」以外のステータスに変更できません」という旨のエラー
                return Optional.of("「貸出中」から「返却済み」以外のステータスに変更できません");
            }

        }

        /*返却済みからのステータス変更の場合のエラー
         */
        //変更前のステータスが返却済み　かつ　変更後のステータスが変更されているかどうか
        if(previousRentalStatus == RentalStatus.RETURNED.getValue() && previousRentalStatus != this.status){

            //「「返却済み」からステータスは変更できません」という旨のエラー
            return Optional.of("「返却済み」からステータスは変更できません");
        }

        /*キャンセルからのステータス変更の場合のエラー
         */
        //変更前のステータスがキャンセル　かつ　変更後のステータスが変更されているかどうか
        if(previousRentalStatus == RentalStatus.CANCELED.getValue() && previousRentalStatus != this.status){

            //「「キャンセル」からステータスは変更できません」という旨のエラー
            return Optional.of("「キャンセル」からステータスは変更できません");
        }

        // 上記の条件に該当しない場合は、デフォルトの空の Optional を返す
        //つまり、エラーがない場合
        return Optional.empty();

    }

    
    //バリデーションチェック
    public Optional<String> validationRentalOn(){

      //変更後のステータスが貸出中だけど現在の日付ではない場合
      if(this.status == RentalStatus.RENTAlING.getValue() && !Timestamp.valueOf(LocalDateTime.now()).equals(this.expectedRentalOn)){

      //「現在の日付で入力してください」という旨のエラー
      return Optional.of("現在の日付で入力してください");
      }

      return Optional.empty();
        
    }


}
