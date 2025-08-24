
const resetPasswordHandler = async (event) => {

    // stop default form action

  event.preventDefault();
  event.stopPropagation();


    // get values of our input fields

  let verificationCode = document.querySelector("#verificationCode").value.trim();
  let newPassword = document.querySelector("#newPassword").value.trim();
  let confirmationPassword = document.querySelector("#confirmPassword").value.trim();

    // validates fields with helper functions

    if(!checkIfInputsExist(verificationCode,newPassword,confirmationPassword) || !validateNewPassword(newPassword,confirmationPassword)){
        return
    }

//    console.log(verificationCode);
//    console.log(newPassword);
//    console.log(confirmationPassword);



    // fetch call to our backend
    // pass field values as JSON
  const response = await fetch("/reset/new/password", {
    method: "POST",
    body: JSON.stringify({ verificationCode, newPassword, confirmationPassword}),
    headers: { "Content-Type": "application/json" },
  });

  //console.log(response.url);

    // check the response (expected "String")
    // retrieve string with .text()
    // redirect the window to that string location
  if (response.ok) {
        const redirectUrl = await response.text(); // or .json() if you return JSON
        window.location.href = redirectUrl;
  }else {
       const redirectUrl = await response.text();
       window.location.href = redirectUrl;
   };


};


// response is a Custom helper function that displays an alert div to inform user of any mistakes

function response(message){

      const alertDiv = document.querySelector("#error");
      const h5EL = document.createElement("h5");
      h5EL.textContent = message;

      alertDiv.append(h5EL);
      alertDiv.style.setProperty('display', 'block');

      setTimeout(() => {
       h5EL.remove(); // Removes the alert element after 2 seconds.
       alertDiv.style.setProperty('display', 'none');
      }, 3000);

      verificationCode.value = "";
      newPassword.value = "";
      confirmPassword.value = "";

      return

}

// checkIfInputsExist is a custom helper function to check if all our inputs exist

function checkIfInputsExist(code, password, confirmPassword){

     let message = "Please Fill All Entries!"

     if (!code || !password || !confirmPassword) {

         response(message);

         return false;
     }

     return true;
}


// validateNewPassword is a custom helper function to validate our password and confirm password fields

function validateNewPassword(password, confirmPassword){

     const regex = /^(?=.*[A-Z])(?=.*\d)(?=.*[!@#$%^&*]).+$/;


    if(password.length < 8){

            let message = "Password must include at least 8 chars long!";
            response(message);

            return false

    }else if (password.length > 15){

         let message = "Password length must be less than 15 chars long!";
         response(message);

         return false

    }else if (password !== confirmPassword) {

        let message = "Passwords Do Not Match!";
        response(message);

         return false;

    }else if(!regex.test(password)){

        let message = "Password must include at least one special char, uppercase letter and number!";
        response(message);

        return false;
    }

    return true;
}




document.addEventListener("DOMContentLoaded", function () {
  document
    .querySelector("#resetBtn")
    .addEventListener("click", resetPasswordHandler);

});

// Above, we wait till the document is loaded before adding any of our listeners.