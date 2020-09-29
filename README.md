
# react-native-paygo

O *react-native-paygo* foi desenvolvido para facilitar a comunicação usando a biblioteca oficial PayGo para android ao *React Native*.
Vale ressaltar que o seu uso é destinado aos produtos do GRUPO ADBRAX, caso ocorra a distribuição deste, não nos resposabilizamos e muito menos oferecemos suporte.

Ainda é necessário que você *desenvolvedor* entre em contato com a equipa de suporta a integração/desenvolvedores para obter as credenciais e liberação do ponto de captura. (PINPAD)

Mauro Moura
mauro.moura@topsapp.com.br


## Instalação (Instrução se público, não está no NPM ainda);

`$ npm install react-native-paygo --save`

### Link Automático

`$ react-native link react-native-paygo`

### Instalação Manual

#### Android

1. Abra `android/app/src/main/java/[...]/MainActivity.java`
  - Adicione `import br.com.paygo.RNPaygoPackage;` nos imports no topo do arquivo.
  - Adicione `new RNPaygoPackage()` na lista de retordo `getPackages()`
2. No final do `android/settings.gradle` adicione:
  	```
  	include ':react-native-paygo'
  	project(':react-native-paygo').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-paygo/android')
  	```
3. Insira dentro do bloco 'dependencies' em `android/app/build.gradle`:
  	```
      compile project(':react-native-paygo')
  	```

## Uso
```javascript
import * as RNPaygo from 'react-native-paygo';
ou
import {
	configurar_dados,
	inicializar_paygo,
	transacao_credito,
	transacao_credito_parecelado,
	transacao_debito,
	obter_comprovante
} from 'react-native-paygo';

//Exemplo

import React, { Component } from 'react'
import {
  Title,
  Container,
  Scroll,
  Button,
  TextButton,
  ContainerInput,
  Input,
  InputLabel
} from './styles'; //Foi utilizado styled components, não presente neste exemplo. 

import * as RNPaygo from 'react-native-paygo';
import { Alert } from 'react-native';

export default class App extends Component {
  constructor(props){
    super(props);
    this.state = {
      nomeAutomacao: "IVE",
      nomeEmpresa: "TopSapp",
      valor: "1000",
	  parcelas: 1,
	  fatura: "1010"
    }
  }
  
  //Ao abrir a aplicação ou respectiva tela, as configurações são aplicatadas e a lib principal é instanciada.
  componentDidMount(){
    RNPaygo.configurar_dados("IVE", "TOPSAPP", "1.0.0", true, true, this.retornoCallback);
    RNPaygo.inicializar_paygo(this.retornoCallback);
  }

// Valida se todos as informações necessárias foram preenchidas. 
  validarDados = () => {
    const {nomeAutomacao, nomeEmpresa, valor, parcelas} = this.state;
    if(
		nomeAutomacao.length > 0 &&
		nomeEmpresa.length > 0 &&
		valor.length > 0 &&
		fatura.length > 0 &&
		parcelas > 0
	){
      return true;
    }else{
      Alert.alert("Atenção!", "Todos os campos devem estar preenchidos!");
      return false;
    }
  }

  obterComprovante = async() => {
   ret = (value) => {
    let valor_tratado;
    valor_tratado = value.replace(/\,/g, "\n");
    valor_tratado = valor_tratado.replace("[", "");
    valor_tratado = valor_tratado.replace("]", "")
    Alert.alert('Comprovante', valor_tratado);
    }
    const comp = await RNPaygo.obter_comprovante(ret);
  }

  retornoCallback = (value) => {
    if(value){
      let valor_tratado = value.replace(/\'/g, '"');
      valor_tratado = JSON.parse(valor_tratado);
      console.log('retornoCallback', valor_tratado);
      if(valor_tratado.status === true && valor_tratado.resultado === "Transacao autorizada"){
        this.obterComprovante();
      }
    }
  }

  venderCredito = async() => {
    const {parcelas, valor, fatura} = this.state;
    const parc = parseInt(parcelas);
    if(this.validarDados()){
      await RNPaygo.transacao_credito(valor, fatura, fatura, this.retornoCallback);
    }
  }
  venderCreditoParc = async() => {
    const {parcelas, valor, fatura} = this.state;
    const parc = parseInt(parcelas);
    if(this.validarDados()){
      await RNPaygo.transacao_credito_parcelado(parc, valor, fatura, fatura, this.retornoCallback);
    }
  }
  venderDebito = async() => {
    const {valor, fatura} = this.state;
    if(this.validarDados()){
      await RNPaygo.transacao_debito(valor, fatura, fatura, this.retornoCallback);
    }
  }

  render() {
    const {nomeAutomacao, nomeEmpresa, valor, parcelas} = this.state;

    return (
      <Scroll>
      <Container>
        <Title>IVE|PAYGO|PINPAD</Title>
        <ContainerInput>
          <InputLabel>Valor</InputLabel>
          <Input value={valor} keyboardType="numeric" onChangeText={(value) => {
            this.setState({valor: value})
          }}/>
        </ContainerInput>
        <ContainerInput>
          <InputLabel>Parcelas</InputLabel>
          <Input value={parcelas.toString()} keyboardType="numeric" onChangeText={(value) => {
            this.setState({parcelas: value})
          }}/>
        </ContainerInput>
        <Button onPress={() => this.venderCredito()}>
          <TextButton>Crédito| A VISTA</TextButton>
        </Button>
        <Button onPress={() => this.venderCreditoParc()}>
          <TextButton>Crédito| PARCELADO</TextButton>
        </Button>
        <Button onPress={() => this.venderDebito()}>
          <TextButton>Débito| A VISTA</TextButton>
        </Button>
        <Button onPress={() => this.obterComprovante()}>
          <TextButton>Visualizar Comprovante</TextButton>
        </Button>
      </Container>
      </Scroll>
    )
  }
}

```

## Documentação

##### Observação:
Os metodos criados na biblioteca principal são *void*, sendo necessário sempre passar um parâmetro para retorno denominado *callback.* Este deve ser uma função de retorno ou tratamento, depende a maneira como você usará o valor retornado. 

Exemplo.
```javascript
// Callback de retorno simples.
function callback(value) {
	console.log(value);
};
  
// Callback de retorno com remoção de "," e "[ ou ]", permitindo a leitura correta do comprovante.
function callbackComprovante(value){
	let comprovante;
	comprovante = value.replace(/\,/g, "\n");
	comprovante = comprovante.replace("[", "");
	comprovante = comprovante.replace("]", "");
	console.log(comprovante);
}
```
---
#### configurar_dados
Para iniciar qualquer transação, é necessário que os dados da automação estejam definidos. Além da instância da biblioteca principal da PayGo.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
| nomeAutomacao      | Sim | string |
| nomeEmpresa      | Sim        |   string |
| versaoAutomacao | Sim        |    string |
|suportaDesconto|Sim| boolean|
|suportaTroco|Sim| boolean|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {configurar_dados} from 'react-native-paygo';
configurar_dados("IVE", "TOPSAPP", "1.0.0", true, true, callback);
```
---
#### inicializar_paygo
Função para instanciar biblioteca principal, responsável por realizar os processos de transação. o *configurar_dados* já deve ter sido chamado.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {inicializar_paygo} from 'react-native-paygo';
inicializar_paygo(callback);
```
---

#### transacao_credito
Função utilizada para realizar uma transação/venda com cartão de crédito á vista.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
|valor|Sim| string|
|fatura|Sim| string|
|fiscal|Sim| string|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {transacao_credito} from 'react-native-paygo';
transacao_credito("1000", "1010", "1010", callback);
```
---

#### transacao_credito_parcelado
Função utilizada para realizar uma transação/venda com cartão de crédito parcelado.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
|parcelas|Sim| number|
|valor|Sim| string|
|fatura|Sim| string|
|fiscal|Sim| string|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {transacao_credito_parcelado} from 'react-native-paygo';
transacao_credito_parcelado(5, "1000", "1010", "1010", callback);
```
---

#### transacao_debito
Função utilizada para realizar uma transação/venda com cartão de débito á vista.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
|valor|Sim| string|
|fatura|Sim| string|
|fiscal|Sim| string|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {transacao_debito} from 'react-native-paygo';
transacao_debito("1000", "1010","1010", callback);
```
---

#### obter_comprovante
Função utilizada para obter o comprovante da ultima transação. Esse é retornado a partir de uma lista de strings. Recomendo usar o *callbackComprovante* de exemplo para teste.

| Parâmetro  | Obrigatório  | Tipo |
| :------------ |:---------------:| -----:|
|callback|Sim| function|

```javascript
//Exemplo de chamada.
import {obter_comprovante} from 'react-native-payg
obter_comprovante(callback);
```
---


  