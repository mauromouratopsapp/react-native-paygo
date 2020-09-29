
import { NativeModules } from 'react-native';
const { RNPaygo } = NativeModules;

const configurar_dados = async(nomeAutomacao: string, nomeEmpresa: string, versaoAutomacao: string, suportaTroco: boolean, suportaDesconto: boolean, callback: any) => {
    await RNPaygo.setDadosAutomacao(nomeAutomacao, nomeEmpresa, versaoAutomacao, suportaDesconto, suportaTroco, callback);
}

const inicializar_paygo = async(callback: any) => {
    await RNPaygo.inicializarPaygo(callback);
}

const transacao_credito_parecelado = async(parcelas: number, valor: string, fatura: string, fiscal: string, callback: any) => {
    await RNPaygo.vendaCredito(parcelas, valor, fatura, fiscal, callback);
}
const transacao_credito = async(valor: string, fatura: string, fiscal: string, callback: any) => {
    await RNPaygo.vendaCredito(1, valor, fatura, fiscal, callback);
}
const transacao_debito = async(valor: string, fiscal: string, callback:any) => {
    await RNPaygo.vendaDebito(valor, fiscal, callback);
}
const obter_comprovante = async(callback: any) => {
    await RNPaygo.obterComprovante(callback);
}

export {
    configurar_dados,
    inicializar_paygo,
    transacao_credito,
    transacao_credito_parecelado,
    transacao_debito,
    obter_comprovante
}