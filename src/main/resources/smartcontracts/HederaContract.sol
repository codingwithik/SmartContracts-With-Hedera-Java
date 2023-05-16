// SPDX-License-Identifier: MIT
pragma solidity ^0.8.7;

import "./HederaTokenService.sol";
import "./ExpiryHelper.sol";

contract CBDC is HederaTokenService, ExpiryHelper {

    uint32 public user_id;

    struct user {
        string regulator;
        string role;
        address userOwner;
        string country;
    }

    //mapping(address => uint) public users;
    mapping(uint32 => user) public users;

    function addUser(string calldata _regulator, string calldata _role, address _userOwner, string calldata _country) public returns(uint32){
        uint32 userId = user_id++;
        users[userId].regulator = _regulator;
        users[userId].role = _role;
        users[userId].userOwner = _userOwner;
        users[userId].country = _country;

        return userId;
    }

    function createToken(uint32 _userId, int64 _initialSupply, int32 _decimals, 
                        string memory _name, string memory _symbol, 
                        int64 _maxSupply, int64 _autoRenewPeriod) external payable returns (address) {
        if(keccak256(abi.encodePacked(users[_userId].role)) == keccak256("cb_user")) {

            IHederaTokenService.HederaToken memory hederaToken;
            hederaToken.name = _name;
            hederaToken.symbol = _symbol;
            hederaToken.treasury = address(this);
            hederaToken.expiry = createAutoRenewExpiry(address(this), _autoRenewPeriod);
            hederaToken.maxSupply = _maxSupply;

            int responseCode;
            address tokenAddress;

            (responseCode, tokenAddress) = createFungibleToken(hederaToken, _initialSupply, _decimals);
            if (responseCode != HederaResponseCodes.SUCCESS) {
                revert ("Token Creation failed!");
            }
            return tokenAddress;
        }
        revert ("Token Creation failed!");
    }

    function mint(uint32 _userId, address _token, int64 _amount) external payable returns(int64) {
        if(keccak256(abi.encodePacked(users[_userId].role)) == keccak256("cb_user")) {
            int responseCode;
            int64 totalSupply;
            int64[] memory metadata;
            bytes[] memory _metadata;

            (responseCode, totalSupply, metadata) = mintToken(_token, _amount, _metadata);
            if (responseCode != HederaResponseCodes.SUCCESS) {
                revert ("Token Minting Failed");
            }
            return totalSupply;
        }
        revert ("You're not permitted to perform this action");
    }

    function burn(uint32 _userId, address _token, int64 _amount) external payable returns(int64){
        if(keccak256(abi.encodePacked(users[_userId].role)) == keccak256("cb_user")) {
            int responseCode;
            int64 totalSupply;
            int64[] memory serialNumbers;

            (responseCode, totalSupply) = burnToken(_token, _amount, serialNumbers);
            if (responseCode != HederaResponseCodes.SUCCESS) {
                revert ("Token Minting Failed");
            }
            return totalSupply;
        }
        revert ("You're not permitted to perform this action");
    }

}