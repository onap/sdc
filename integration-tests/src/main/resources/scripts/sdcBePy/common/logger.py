import sys

debugFlag = True


def join_strings(lst):
    return ''.join([str(string) for string in lst])


def debug(desc, *args):
    if debugFlag:
        print(desc, join_strings(args))


def log(desc, arg=None):
    if arg:
        print(desc, arg)
    else:
        print(desc)


def print_and_exit(error_code, error_desc):
    if error_code > 0:
        print("status={0}. {1}".format(error_code, '' if not error_desc else error_desc))
    else:
        print("status={0}".format(error_code))
    sys.exit(error_code)


def print_name_and_return_code(name, code, with_line=True):
    if _strings_correct(name, code):
        if with_line:
            print("----------------------------------------")
        print("{0:30} | {1:6}".format(name, code))
        if with_line:
            print("----------------------------------------")
    else:
        print("name of the item or return code from request is none -> error occurred!!")


def _strings_correct(*strings):
    results = [(string is not None and string != "") for string in strings]
    return all(results) is True
