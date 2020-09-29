package br.com.paygo;

import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import android.os.Handler;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import br.com.setis.interfaceautomacao.AplicacaoNaoInstaladaExcecao;
import br.com.setis.interfaceautomacao.Cartoes;
import br.com.setis.interfaceautomacao.Confirmacoes;
import br.com.setis.interfaceautomacao.DadosAutomacao;
import br.com.setis.interfaceautomacao.EntradaTransacao;
import br.com.setis.interfaceautomacao.Financiamentos;
import br.com.setis.interfaceautomacao.Operacoes;
import br.com.setis.interfaceautomacao.Personalizacao;
import br.com.setis.interfaceautomacao.Provedores;
import br.com.setis.interfaceautomacao.QuedaConexaoTerminalExcecao;
import br.com.setis.interfaceautomacao.SaidaTransacao;
import br.com.setis.interfaceautomacao.TerminalNaoConfiguradoExcecao;
import br.com.setis.interfaceautomacao.Transacoes;


public class RNPaygoModule extends ReactContextBaseJavaModule {
  private final ReactApplicationContext reactContext;

  private static Transacoes mTransacoes;
  private static Handler mHandler;
  private static Confirmacoes mConfirmacao;
  private static String mensagem = null;
  private EntradaTransacao mEntradaTransacao = null;
  private SaidaTransacao mSaidaTransacao;
  private DadosAutomacao mDadosAutomacao;
  private boolean suportaViaDifer = false;
  private boolean suportaViaReduz = false;

  //Dados Automação
  private String nome_empresa = "";
  private String nome_automacao = "";
  private String versao_automacao = "";
  private Boolean suporta_troco = false;
  private Boolean suporta_desconto = false;

  //Retorno venda
  private List<String> comprovante;

  public RNPaygoModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  /*
  * Método que define o nome do Pacote
  */
  @Override
  public String getName() {
    return "RNPaygo";
  }

  /*
   * Método responsável pelas configurações inicias do pacote.
   */
  @ReactMethod
  public void setDadosAutomacao(String nomeEmpresa, String nomeAutomacao, String versaoAutomacao, Boolean suportaTroco, Boolean suportaDesconto, Callback callback){
    try {
      this.nome_empresa = nomeEmpresa;
      this.nome_automacao = nomeAutomacao;
      this.versao_automacao = versaoAutomacao;
      this.suporta_troco = suportaTroco;
      this.suporta_desconto = suportaDesconto;

      callback.invoke("{"+"'resultado':'Configurado.',"+"'status':true}");
    }catch (Exception e){
      callback.invoke("{"+"'resultado':'Não Configurado.',"+"'status':false}");
    }
  }

  /*
   * Método responsável por instanciar a biblioteca PAYGO.
   */
  @ReactMethod
  public void inicializarPaygo(Callback callback){
    try {
      mDadosAutomacao = new DadosAutomacao(this.nome_empresa, this.nome_automacao, this.versao_automacao, this.suporta_troco, this.suporta_desconto, this.suportaViaDifer, this.suportaViaReduz, null);
      mTransacoes = Transacoes.obtemInstancia(mDadosAutomacao, this.reactContext);

      callback.invoke("{"+"'resultado':'PG Iniciado.',"+"'status':true}");;
    }catch (Exception e){
      callback.invoke("{"+"'resultado':'Não iniciado.',"+"'status':false}");
    }
  }

  /*
   * Método responsável por efetuar uma transação com CRÉDITO, sendo ela A VISTA ou PARCELADO.
   */
  @ReactMethod
  public void vendaCredito(Integer parcelas, String valor, String fiscal, String fatura, final Callback callback){
    final String[] final_response = new String[1];

    mEntradaTransacao = new EntradaTransacao(Operacoes.VENDA, String.valueOf(new Random().nextLong()));
    mEntradaTransacao.informaDocumentoFiscal(fiscal);
    mEntradaTransacao.informaValorTotal(valor);
    mEntradaTransacao.informaCodigoMoeda("986");
    mEntradaTransacao.informaNumeroFatura(fatura);

    mEntradaTransacao.informaTipoCartao(Cartoes.CARTAO_CREDITO);
    if(parcelas > 1){
      mEntradaTransacao.informaTipoFinanciamento(Financiamentos.PARCELADO_EMISSOR);
      mEntradaTransacao.informaNumeroParcelas(parcelas);
    }else{
      mEntradaTransacao.informaTipoFinanciamento(Financiamentos.A_VISTA);
    }

    mConfirmacao = new Confirmacoes();

    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          mSaidaTransacao = mTransacoes
                  .realizaTransacao(mEntradaTransacao);
          mConfirmacao
                  .informaIdentificadorConfirmacaoTransacao(mSaidaTransacao
                          .obtemIdentificadorConfirmacaoTransacao());

        } catch (QuedaConexaoTerminalExcecao e) {
          mensagem = "Queda de conexão com o terminal!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } catch (TerminalNaoConfiguradoExcecao e) {
          mensagem = "Cliente não configurado!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } catch (AplicacaoNaoInstaladaExcecao e) {
          mensagem = "Aplicação não instalada!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } finally {

          mEntradaTransacao = null;

          if(mSaidaTransacao.obtemInformacaoConfirmacao() == true){
            if(mSaidaTransacao.obtemResultadoTransacao() == 0){
              final_response[0] = "{"+"'resultado':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status':true, 'comprovante': true}";
              setComprovante(mSaidaTransacao.obtemComprovanteCompleto());
              callback.invoke(final_response[0]);
            }else{
              final_response[0] = "{"+"'resultado':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status':false, 'comprovante': false}";
              callback.invoke(final_response[0]);
            }
          }else{
            final_response[0] = "{"+"'resultado:':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status':false, 'comprovante': false}";
            callback.invoke(final_response[0]);
          }
          //          System.out.println(mSaidaTransacao.obtemResultadoTransacao());
          //          System.out.println(mSaidaTransacao.obtemMensagemResultado());
        }
      }
    }).start();
  }

  /*
   * Método responsável por efetuar uma transação com DÉBITO A VISTA.
   */
  @ReactMethod
  public void vendaDebito(String valor, String fiscal, final Callback callback){
    final String[] final_response = new String[1];

    mEntradaTransacao = new EntradaTransacao(Operacoes.VENDA, String.valueOf(new Random().nextLong()));
    mEntradaTransacao.informaDocumentoFiscal(fiscal);
    mEntradaTransacao.informaValorTotal(valor);
    mEntradaTransacao.informaCodigoMoeda("986");

    mEntradaTransacao.informaTipoCartao(Cartoes.CARTAO_DEBITO);
    mEntradaTransacao.informaTipoFinanciamento(Financiamentos.A_VISTA);

    mConfirmacao = new Confirmacoes();

    new Thread(new Runnable() {

      @Override
      public void run() {
        try {
          mSaidaTransacao = mTransacoes
                  .realizaTransacao(mEntradaTransacao);
          mConfirmacao
                  .informaIdentificadorConfirmacaoTransacao(mSaidaTransacao
                          .obtemIdentificadorConfirmacaoTransacao());

        } catch (QuedaConexaoTerminalExcecao e) {
          mensagem = "Queda de conexão com o terminal!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } catch (TerminalNaoConfiguradoExcecao e) {
          mensagem = "Cliente não configurado!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } catch (AplicacaoNaoInstaladaExcecao e) {
          mensagem = "Aplicação não instalada!";
          final_response[0] = "{"+"'resultado:':'"+mensagem+"',"+"'status':false, 'comprovante': false}";
          callback.invoke(final_response[0]);
        } finally {

          mEntradaTransacao = null;

          if(mSaidaTransacao.obtemInformacaoConfirmacao() == true){
            if(mSaidaTransacao.obtemResultadoTransacao() == 0){
              final_response[0] = "{"+"'resultado':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status': true, 'comprovante': true}";
              setComprovante(mSaidaTransacao.obtemComprovanteCompleto());
              callback.invoke(final_response[0]);
            }else{
              final_response[0] = "{"+"'resultado':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status':false, 'comprovante': false}";
              callback.invoke(final_response[0]);
            }
          }else{
            final_response[0] = "{"+"'resultado:':'"+mSaidaTransacao.obtemMensagemResultado().trim()+"',"+"'status':false, 'comprovante': false}";
            callback.invoke(final_response[0]);
          }

        }
      }
    }).start();
  }

  /*
   * Método responsável por salvar o comprovante;
   */
  private void setComprovante(List<String> obtemComprovanteCompleto) {
    this.comprovante = obtemComprovanteCompleto;
  }

  /*
   * Método responsável por obter o comprovante salvo durante uma transação que obteve sucesso.
   */
  @ReactMethod
  public void obterComprovante(Callback callback){
    String teste = this.comprovante.toString();
    callback.invoke(teste);
  }
}
