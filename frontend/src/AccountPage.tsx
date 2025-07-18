import { useState } from 'react';

function AccountPage() {
  const [balanceResult, setBalanceResult] = useState<any>(null);
  const [transactionsResult, setTransactionsResult] = useState<any>(null);
  const [detailsResult, setDetailsResult] = useState<any>(null);

  // API 응답 필드명과 한글명 매핑
  const keyToKoreanNameMap: { [key: string]: string } = {
    // 공통 API 응답 필드
    return_code: '리턴코드',
    return_msg: '리턴메시지',
    token: '접근토큰',
    expires_dt: '만료일시',
    token_type: '토큰타입',
    api_id: 'API ID',
    cont_yn: '연속조회여부',
    next_key: '연속조회키',

    // 계좌 관련 공통 필드 (kt00018, kt00004, ka10085 등에서 공통으로 나타날 수 있는 필드)
    stk_cd: '종목번호',
    stk_nm: '종목명',
    cur_prc: '현재가',
    pur_pric: '매입가',
    pur_amt: '매입금액',
    rmnd_qty: '보유수량',
    tdy_sel_pl: '당일매도손익',
    tdy_trde_cmsn: '당일매매수수료',
    tdy_trde_tax: '당일매매세금',
    loan_dt: '대출일',
    setl_remn: '결제잔고',
    clrn_alow_qty: '청산가능수량',
    crd_amt: '신용금액',
    crd_int: '신용이자',
    expr_dt: '만기일',
    dt: '일자',

    // kt00018 (계좌평가잔고내역요청) 특정 필드 (최상위 필드)
    tot_pur_amt: '총매입금액',
    tot_evlt_amt: '총평가금액',
    tot_evlt_pl: '총평가손익금액',
    tot_prft_rt: '총수익률(%)',
    prsm_dpst_aset_amt: '추정예탁자산',
    tot_loan_amt: '총대출금',
    tot_crd_loan_amt: '총융자금액',
    tot_crd_ls_amt: '총대주금액',
    acnt_evlt_remn_indv_tot: '종목별계좌평가현황',

    // kt00004 (계좌평가현황요청) 특정 필드 (최상위 필드)
    acnt_nm: '계좌명',
    brch_nm: '지점명',
    entr: '예수금',
    d2_entra: 'D+2추정예수금',
    tot_est_amt: '총추정예탁자산',
    aset_evlt_amt: '예탁자산평가액',
    tot_grnt_sella: '매도담보대출금',
    tdy_lspft_amt: '당일투자원금',
    invt_bsamt: '당월투자원금',
    lspft_amt: '누적투자원금',
    tdy_lspft: '당일투자손익',
    lspft2: '당일투자손익(2)',
    lspft_rt: '당일손익율',
    lspft_ratio: '당월손익율(비율)',
    stk_acnt_evlt_prst: '종목별계좌평가현황',

    // ka10085 (계좌수익률요청) 특정 필드 (최상위 필드)
    acnt_prft_rt: '계좌수익률',

    // 리스트 내부 필드 (kt00018, kt00004, ka10085의 리스트 내부에 나타날 수 있는 필드)
    evltv_prft: '평가손익',
    prft_rt: '수익률',
    poss_rt: '보유비중(%)',
    crd_tp: '신용구분',
    crd_tp_nm: '신용구분명',
    sum_cmsn: '수수료합',
    sell_cmsn: '평가수수료',
    tax: '세금',
    trde_able_qty: '매매가능수량',
    pred_buyq: '전일매수수량',
    pred_sellq: '전일매도수량',
    tdy_buyq: '금일매수수량',
    tdy_sellq: '금일매도수량',
  };

  const fetchData = async (url: string, setResult: (data: any) => void) => {
    try {
      const response = await fetch(url, {
        method: 'POST',
      });
      const data = await response.json(); // JSON으로 파싱
      setResult(data);
    } catch (error) {
      console.error(`Error fetching from ${url}:`, error);
      setResult({ error: `Error fetching from ${url}.` });
    }
  };

  const handleGetBalance = () => fetchData('/api/account/balance', setBalanceResult);
  const handleGetTransactions = () => fetchData('/api/account/transactions', setTransactionsResult);
  const handleGetDetails = () => fetchData('/api/account/details', setDetailsResult);

  const renderTable = (data: any) => {
    if (!data) return null;
    if (data.error) return <p style={{ color: 'red' }}>{data.error}</p>;

    // 데이터가 배열인 경우 (여러 항목)
    if (Array.isArray(data)) {
      if (data.length === 0) return <p>No data available.</p>;
      const headers = Object.keys(data[0]);
      return (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
          <thead>
            <tr>
              {headers.map((header) => (
                <th key={header} style={{ border: '1px solid #ddd', padding: '8px', backgroundColor: '#f2f2f2' }}>
                  {keyToKoreanNameMap[header] || header} ({header})
                </th>
              ))}
            </tr>
          </thead>
          <tbody>
            {data.map((row, rowIndex) => (
              <tr key={rowIndex}>
                {headers.map((header) => (
                  <td key={header} style={{ border: '1px solid #ddd', padding: '8px' }}>
                    {JSON.stringify(row[header])}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      );
    }

    // 데이터가 단일 객체인 경우
    if (typeof data === 'object') {
      const headers = Object.keys(data);
      return (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
          <thead>
            <tr>
              <th style={{ border: '1px solid #ddd', padding: '8px', backgroundColor: '#f2f2f2' }}>한글명 (Key)</th>
              <th style={{ border: '1px solid #ddd', padding: '8px', backgroundColor: '#f2f2f2' }}>Value</th>
            </tr>
          </thead>
          <tbody>
            {headers.map((key) => (
              <tr key={key}>
                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{keyToKoreanNameMap[key] || key} ({key})</td>
                <td style={{ border: '1px solid #ddd', padding: '8px' }}>{JSON.stringify(data[key])}</td>
              </tr>
            ))}
          </tbody>
        </table>
      );
    }

    return <p>Invalid data format.</p>;
  };

  return (
    <div style={{ textAlign: 'center', padding: '20px' }}>
      <h1>Account Information</h1>
      <div style={{ marginBottom: '20px' }}>
        <button onClick={handleGetBalance} style={{ margin: '5px', padding: '10px 20px', backgroundColor: '#007bff', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
          Get Account Balance (kt00018)
        </button>
        {renderTable(balanceResult)}
      </div>

      <div style={{ marginBottom: '20px' }}>
        <button onClick={handleGetTransactions} style={{ margin: '5px', padding: '10px 20px', backgroundColor: '#28a745', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
          Get Account Transactions (kt00004)
        </button>
        {renderTable(transactionsResult)}
      </div>

      <div style={{ marginBottom: '20px' }}>
        <button onClick={handleGetDetails} style={{ margin: '5px', padding: '10px 20px', backgroundColor: '#ffc107', color: 'white', border: 'none', borderRadius: '5px', cursor: 'pointer' }}>
          Get Account Details (ka10085)
        </button>
        {renderTable(detailsResult)}
      </div>
    </div>
  );
}

export default AccountPage;
