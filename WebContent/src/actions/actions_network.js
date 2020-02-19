export const RESET_NETWORK_STATUSES = 'RESET_NETWORK_STATUSES';

/*
    Acção que serve simplesmente para notificar todos os
    elementos do estado que contém dados sobre o estado
    dos pedidos à rede que podem fazer reset a essas
    propriedades.
*/
export function resetNetworkStatuses() {
    return {
        type: RESET_NETWORK_STATUSES
    };
}